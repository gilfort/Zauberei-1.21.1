
        package com.gilfort.zauberei.guis;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armorbonus.ZaubereiReloadListener;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetDataRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sets Manager GUI — a visual interface for browsing, inspecting,
 * validating and managing armor set effect definitions.
 *
 * <p>Uses a configbook.png background (1600×1024 original resolution)
 * displayed at 75% of the player's screen, dynamically scaled to
 * maintain aspect ratio regardless of GUI scale setting.</p>
 *
 * <p>Left page: scrollable list of all loaded set definitions.
 * Right page: detail view of the currently selected set.</p>
 *
 * @see ArmorSetDataRegistry
 * @see ZaubereiReloadListener
 */
@OnlyIn(Dist.CLIENT)
public class SetsManagerScreen extends Screen {

    // ─── Texture ─────────────────────────────────────────────────────────
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Zauberei.MODID, "textures/gui/configbook.png");

    /** Original texture dimensions (used for UV mapping). */
    private static final int TEX_WIDTH = 1600;
    private static final int TEX_HEIGHT = 1024;

    /** Target fill: the book occupies this fraction of the screen. */
    private static final float TARGET_FILL = 0.75f;

    // ─── Page area fractions (relative to texture dimensions) ────────────
    // These define where the usable page area starts/ends as a fraction
    // of the full texture. Derived from the configbook.png layout.

    // Left page content area (fractional)
    private static final float LEFT_X_FRAC  = 110f / TEX_WIDTH;    // left edge of left page
    private static final float LEFT_W_FRAC  = 580f / TEX_WIDTH;    // width of left page
    private static final float LEFT_Y_FRAC  = 80f / TEX_HEIGHT;    // top edge
    private static final float LEFT_H_FRAC  = 800f / TEX_HEIGHT;   // height of content area

    // Right page content area (fractional)
    private static final float RIGHT_X_FRAC = 870f / TEX_WIDTH;    // left edge of right page
    private static final float RIGHT_W_FRAC = 580f / TEX_WIDTH;    // width of right page
    private static final float RIGHT_Y_FRAC = 80f / TEX_HEIGHT;    // top edge
    private static final float RIGHT_H_FRAC = 800f / TEX_HEIGHT;   // height of content area

    // ─── Scope colors ────────────────────────────────────────────────────
    private static final int COLOR_SPECIFIC  = 0xFF55FF55;  // Green
    private static final int COLOR_ALL_MAJOR = 0xFF5555FF;  // Blue
    private static final int COLOR_UNIVERSAL = 0xFFFFFF55;  // Yellow (accessibility)
    private static final int COLOR_HEADER    = 0xFF553300;  // Dark brown (book theme)
    private static final int COLOR_TEXT      = 0xFF332200;  // Dark brown text
    private static final int COLOR_EFFECT    = 0xFF7733AA;  // Purple for effects
    private static final int COLOR_ATTRIBUTE = 0xFF338833;  // Green for attributes
    private static final int COLOR_GRAY      = 0xFF888888;  // Gray hints
    private static final int COLOR_SELECTED  = 0x44FFCC00;  // Selection highlight (translucent gold)
    private static final int COLOR_HOVER     = 0x22FFCC00;  // Hover highlight

    // ─── Dynamic layout (computed in init()) ─────────────────────────────
    private int renderWidth, renderHeight;
    private int guiLeft, guiTop;
    private int leftX, leftY, leftW, leftH;
    private int rightX, rightY, rightW, rightH;
    private int lineHeight;
    private int maxLinesLeft, maxLinesRight;

    // ─── Data ────────────────────────────────────────────────────────────
    private List<ListEntry> listEntries = new ArrayList<>();
    private int selectedIndex = -1;
    private int leftScrollOffset = 0;
    private int rightScrollOffset = 0;

    // ─── Validation state ────────────────────────────────────────────────
    private boolean showingValidation = false;
    private List<ZaubereiReloadListener.ValidationResult> validationResults = null;

    // ─── Inner types ─────────────────────────────────────────────────────

    /**
     * Represents one row in the left-side list.
     * Can be either a TAG header (grouping) or a SCOPE entry (clickable).
     */
    private record ListEntry(EntryType type, String tag, String scopeLabel,
                             String major, int year, ArmorSetData data) {
        enum EntryType { TAG_HEADER, SCOPE_ENTRY }

        int getColor() {
            if (type == EntryType.TAG_HEADER) return COLOR_HEADER;
            boolean wildMajor = ArmorSetDataRegistry.WILDCARD_MAJOR.equals(major);
            boolean wildYear = year == ArmorSetDataRegistry.WILDCARD_YEAR;
            if (wildMajor && wildYear) return COLOR_UNIVERSAL;
            if (wildMajor) return COLOR_ALL_MAJOR;
            return COLOR_SPECIFIC;
        }

        String getDisplayText() {
            if (type == EntryType.TAG_HEADER) {
                // Format tag as readable name
                return formatTagName(tag);
            }
            return "  \u25CF " + scopeLabel; // ● bullet + scope
        }

        private static String formatTagName(String tagString) {
            try {
                ResourceLocation loc = ResourceLocation.parse(tagString);
                String path = loc.getPath();
                path = path.replaceAll("_(armou?rs?|set|equipment|gear)$", "");
                String[] words = path.split("_");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) sb.append(word.substring(1));
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                return tagString;
            }
        }
    }

    // ─── Constructor ─────────────────────────────────────────────────────

    public SetsManagerScreen() {
        super(Component.literal("Sets Manager"));
    }

    // ─── Init ────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        // ── Dynamic scaling: 75% of screen, maintain aspect ratio ────────
        float aspectRatio = (float) TEX_WIDTH / TEX_HEIGHT;

        int maxW = (int) (this.width * TARGET_FILL);
        int maxH = (int) (this.height * TARGET_FILL);

        if (maxW / aspectRatio <= maxH) {
            renderWidth = maxW;
            renderHeight = (int) (maxW / aspectRatio);
        } else {
            renderHeight = maxH;
            renderWidth = (int) (maxH * aspectRatio);
        }

        guiLeft = (this.width - renderWidth) / 2;
        guiTop = (this.height - renderHeight) / 2;

        // ── Compute page areas from fractions ────────────────────────────
        leftX = guiLeft + (int) (LEFT_X_FRAC * renderWidth);
        leftY = guiTop + (int) (LEFT_Y_FRAC * renderHeight);
        leftW = (int) (LEFT_W_FRAC * renderWidth);
        leftH = (int) (LEFT_H_FRAC * renderHeight);

        rightX = guiLeft + (int) (RIGHT_X_FRAC * renderWidth);
        rightY = guiTop + (int) (RIGHT_Y_FRAC * renderHeight);
        rightW = (int) (RIGHT_W_FRAC * renderWidth);
        rightH = (int) (RIGHT_H_FRAC * renderHeight);

        // ── Line height scales with book size ────────────────────────────
        lineHeight = Math.max(10, renderHeight / 25);
        maxLinesLeft = leftH / lineHeight;
        maxLinesRight = rightH / lineHeight;

        // ── Build list data ──────────────────────────────────────────────
        buildListEntries();

        // ── Buttons ──────────────────────────────────────────────────────
        int buttonW = 60;
        int buttonH = 20;
        int buttonY = guiTop + renderHeight - 35;
        int buttonSpacing = 10;
        int totalButtonsW = buttonW * 3 + buttonSpacing * 2;
        int buttonStartX = (this.width - totalButtonsW) / 2;

        addRenderableWidget(Button.builder(Component.literal("Reload"), btn -> onReload())
                .bounds(buttonStartX, buttonY, buttonW, buttonH)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Validate"), btn -> onValidate())
                .bounds(buttonStartX + buttonW + buttonSpacing, buttonY, buttonW, buttonH)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
                .bounds(buttonStartX + (buttonW + buttonSpacing) * 2, buttonY, buttonW, buttonH)
                .build());
    }

    // ─── Data building ───────────────────────────────────────────────────

    private void buildListEntries() {
        listEntries.clear();
        selectedIndex = -1;
        leftScrollOffset = 0;
        rightScrollOffset = 0;
        showingValidation = false;
        validationResults = null;

        List<ArmorSetDataRegistry.SetEntry> allEntries = ArmorSetDataRegistry.getAllEntries();

        // Group by tag, then sort entries within each group
        Map<String, List<ArmorSetDataRegistry.SetEntry>> grouped = allEntries.stream()
                .collect(Collectors.groupingBy(ArmorSetDataRegistry.SetEntry::tag,
                        LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<ArmorSetDataRegistry.SetEntry>> group : grouped.entrySet()) {
            String tag = group.getKey();

            // Add tag header
            listEntries.add(new ListEntry(
                    ListEntry.EntryType.TAG_HEADER, tag, "", "", 0, null));

            // Add scope entries, sorted: universal → all_majors → specific
            List<ArmorSetDataRegistry.SetEntry> sorted = group.getValue().stream()
                    .sorted((a, b) -> {
                        int prioA = scopePriority(a);
                        int prioB = scopePriority(b);
                        return Integer.compare(prioA, prioB);
                    })
                    .toList();

            for (ArmorSetDataRegistry.SetEntry se : sorted) {
                listEntries.add(new ListEntry(
                        ListEntry.EntryType.SCOPE_ENTRY, tag,
                        se.scopeLabel(), se.major(), se.year(), se.data()));
            }
        }

        // Auto-select first scope entry
        for (int i = 0; i < listEntries.size(); i++) {
            if (listEntries.get(i).type() == ListEntry.EntryType.SCOPE_ENTRY) {
                selectedIndex = i;
                break;
            }
        }
    }

    private static int scopePriority(ArmorSetDataRegistry.SetEntry e) {
        boolean wildMajor = ArmorSetDataRegistry.WILDCARD_MAJOR.equals(e.major());
        boolean wildYear = e.year() == ArmorSetDataRegistry.WILDCARD_YEAR;
        if (wildMajor && wildYear) return 0; // universal first
        if (wildMajor) return 1;             // all_majors second
        return 2;                            // specific last
    }

    // ─── Rendering ───────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dim background
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // ── Draw book texture (stretched to renderWidth × renderHeight) ──
        graphics.blit(BACKGROUND,
                guiLeft, guiTop,                    // screen position
                renderWidth, renderHeight,          // render size
                0, 0,                               // UV start
                TEX_WIDTH, TEX_HEIGHT,              // UV size
                TEX_WIDTH, TEX_HEIGHT);             // texture file size

        // ── Draw left page (set list) ────────────────────────────────────
        renderLeftPage(graphics, mouseX, mouseY);

        // ── Draw right page (details or validation) ──────────────────────
        if (showingValidation && validationResults != null) {
            renderValidationPage(graphics);
        } else {
            renderRightPage(graphics);
        }

        // ── Title on top of book ─────────────────────────────────────────
        Component title = Component.literal("Set Effects Manager")
                .withStyle(s -> s.withBold(true));
        int titleWidth = this.font.width(title);
        graphics.drawString(this.font, title,
                guiLeft + renderWidth / 2 - titleWidth / 2,
                guiTop + (int) (30f / TEX_HEIGHT * renderHeight),
                COLOR_HEADER, false);

        // ── Render widgets (buttons) on top ──────────────────────────────
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ─── Left page: Set list ─────────────────────────────────────────────

    private void renderLeftPage(GuiGraphics graphics, int mouseX, int mouseY) {
        if (listEntries.isEmpty()) {
            graphics.drawString(this.font,
                    Component.literal("No sets loaded.").withStyle(ChatFormatting.ITALIC),
                    leftX + 5, leftY + 5, COLOR_GRAY, false);
            graphics.drawString(this.font,
                    Component.literal("Use /zauberei sets create"),
                    leftX + 5, leftY + 5 + lineHeight, COLOR_GRAY, false);
            return;
        }

        // Scissor to left page area
        graphics.enableScissor(leftX, leftY, leftX + leftW, leftY + leftH);

        int y = leftY;
        int visibleStart = leftScrollOffset;
        int visibleEnd = Math.min(listEntries.size(), leftScrollOffset + maxLinesLeft);

        for (int i = visibleStart; i < visibleEnd; i++) {
            ListEntry entry = listEntries.get(i);
            int entryY = y + (i - visibleStart) * lineHeight;

            // Selection highlight
            if (i == selectedIndex) {
                graphics.fill(leftX, entryY - 1, leftX + leftW, entryY + lineHeight - 1, COLOR_SELECTED);
            }
            // Hover highlight (only for clickable entries)
            else if (entry.type() == ListEntry.EntryType.SCOPE_ENTRY
                    && mouseX >= leftX && mouseX <= leftX + leftW
                    && mouseY >= entryY && mouseY < entryY + lineHeight) {
                graphics.fill(leftX, entryY - 1, leftX + leftW, entryY + lineHeight - 1, COLOR_HOVER);
            }

            // Draw text
            String text = entry.getDisplayText();
            // Truncate if too wide
            if (this.font.width(text) > leftW - 10) {
                while (this.font.width(text + "...") > leftW - 10 && text.length() > 3) {
                    text = text.substring(0, text.length() - 1);
                }
                text += "...";
            }

            boolean bold = entry.type() == ListEntry.EntryType.TAG_HEADER;
            if (bold) {
                graphics.drawString(this.font,
                        Component.literal(text).withStyle(s -> s.withBold(true)),
                        leftX + 3, entryY, entry.getColor(), false);
            } else {
                graphics.drawString(this.font, text, leftX + 3, entryY, entry.getColor(), false);
            }
        }

        // ── Scrollbar indicator ──────────────────────────────────────────
        if (listEntries.size() > maxLinesLeft) {
            int scrollBarX = leftX + leftW - 3;
            int scrollBarHeight = leftH;
            float ratio = (float) leftScrollOffset / (listEntries.size() - maxLinesLeft);
            int thumbHeight = Math.max(10, scrollBarHeight * maxLinesLeft / listEntries.size());
            int thumbY = leftY + (int) ((scrollBarHeight - thumbHeight) * ratio);

            graphics.fill(scrollBarX, leftY, scrollBarX + 2, leftY + scrollBarHeight, 0x33000000);
            graphics.fill(scrollBarX, thumbY, scrollBarX + 2, thumbY + thumbHeight, 0xAA553300);
        }

        graphics.disableScissor();
    }

    // ─── Right page: Set details ─────────────────────────────────────────

    private void renderRightPage(GuiGraphics graphics) {
        graphics.enableScissor(rightX, rightY, rightX + rightW, rightY + rightH);

        if (selectedIndex < 0 || selectedIndex >= listEntries.size()) {
            graphics.drawString(this.font,
                    Component.literal("Select a set on the left.").withStyle(ChatFormatting.ITALIC),
                    rightX + 5, rightY + 5, COLOR_GRAY, false);
            graphics.disableScissor();
            return;
        }

        ListEntry selected = listEntries.get(selectedIndex);
        if (selected.data() == null) {
            graphics.drawString(this.font,
                    Component.literal("(Header — select an entry below)"),
                    rightX + 5, rightY + 5, COLOR_GRAY, false);
            graphics.disableScissor();
            return;
        }

        // Build detail lines
        List<DetailLine> detailLines = buildDetailLines(selected);

        int y = rightY - rightScrollOffset * lineHeight;
        for (DetailLine line : detailLines) {
            if (y >= rightY - lineHeight && y < rightY + rightH) {
                if (line.bold) {
                    graphics.drawString(this.font,
                            Component.literal(line.text).withStyle(s -> s.withBold(true)),
                            rightX + line.indent + 3, y, line.color, false);
                } else {
                    graphics.drawString(this.font, line.text,
                            rightX + line.indent + 3, y, line.color, false);
                }
            }
            y += lineHeight;
        }

        // Scrollbar
        if (detailLines.size() > maxLinesRight) {
            int scrollBarX = rightX + rightW - 3;
            float ratio = (float) rightScrollOffset / (detailLines.size() - maxLinesRight);
            int thumbHeight = Math.max(10, rightH * maxLinesRight / detailLines.size());
            int thumbY = rightY + (int) ((rightH - thumbHeight) * ratio);

            graphics.fill(scrollBarX, rightY, scrollBarX + 2, rightY + rightH, 0x33000000);
            graphics.fill(scrollBarX, thumbY, scrollBarX + 2, thumbY + thumbHeight, 0xAA553300);
        }

        graphics.disableScissor();
    }

    // ─── Detail line builder ─────────────────────────────────────────────

    private record DetailLine(String text, int color, int indent, boolean bold) {
        static DetailLine header(String text) {
            return new DetailLine(text, COLOR_HEADER, 0, true);
        }
        static DetailLine text(String text, int color) {
            return new DetailLine(text, color, 0, false);
        }
        static DetailLine indented(String text, int color) {
            return new DetailLine(text, color, 8, false);
        }
        static DetailLine blank() {
            return new DetailLine("", 0, 0, false);
        }
    }

    private List<DetailLine> buildDetailLines(ListEntry entry) {
        List<DetailLine> lines = new ArrayList<>();
        ArmorSetData data = entry.data();

        // ── Set name ─────────────────────────────────────────────────────
        String displayName = data.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = ListEntry.formatTagName(entry.tag());
        }
        lines.add(DetailLine.header(displayName));
        lines.add(DetailLine.blank());

        // ── Tag & scope ──────────────────────────────────────────────────
        lines.add(DetailLine.text("Tag: " + entry.tag(), COLOR_TEXT));
        lines.add(DetailLine.text("Scope: " + entry.scopeLabel(), entry.getColor()));
        lines.add(DetailLine.blank());

        // ── Thresholds ───────────────────────────────────────────────────
        if (data.getParts() == null || data.getParts().isEmpty()) {
            lines.add(DetailLine.text("(No thresholds defined)", COLOR_GRAY));
            return lines;
        }

        // Sort part keys numerically
        List<Map.Entry<String, ArmorSetData.PartData>> sortedParts = data.getParts().entrySet()
                .stream()
                .sorted(Comparator.comparingInt(e -> {
                    try { return Integer.parseInt(e.getKey().replace("Part", "")); }
                    catch (NumberFormatException ex) { return 99; }
                }))
                .toList();

        for (Map.Entry<String, ArmorSetData.PartData> partEntry : sortedParts) {
            String partKey = partEntry.getKey();
            ArmorSetData.PartData partData = partEntry.getValue();

            // Threshold header
            String num = partKey.replace("Part", "");
            lines.add(DetailLine.header("\u2550\u2550 " + num + " Piece" +
                    (Integer.parseInt(num) > 1 ? "s" : "") + " \u2550\u2550"));

            // Effects
            if (partData.getEffects() != null && !partData.getEffects().isEmpty()) {
                for (ArmorSetData.EffectData effect : partData.getEffects()) {
                    String effectName = resolveEffectName(effect.getEffect());
                    int level = effect.getAmplifier() + 1;
                    String roman = toRoman(level);
                    lines.add(DetailLine.indented("\u2726 " + effectName + " " + roman, COLOR_EFFECT));
                }
            }

            // Attributes
            if (partData.getAttributes() != null && !partData.getAttributes().isEmpty()) {
                for (Map.Entry<String, ArmorSetData.AttributeData> attr : partData.getAttributes().entrySet()) {
                    String attrName = resolveAttributeName(attr.getKey());
                    ArmorSetData.AttributeData attrData = attr.getValue();
                    String valueStr = formatAttributeValue(attrData);
                    lines.add(DetailLine.indented("\u25B8 " + attrName + " " + valueStr, COLOR_ATTRIBUTE));
                }
            }

            // No effects and no attributes?
            if ((partData.getEffects() == null || partData.getEffects().isEmpty())
                    && (partData.getAttributes() == null || partData.getAttributes().isEmpty())) {
                lines.add(DetailLine.indented("(no bonuses)", COLOR_GRAY));
            }

            lines.add(DetailLine.blank());
        }

        return lines;
    }

    // ─── Validation page ─────────────────────────────────────────────────

    private void renderValidationPage(GuiGraphics graphics) {
        graphics.enableScissor(rightX, rightY, rightX + rightW, rightY + rightH);

        List<DetailLine> lines = new ArrayList<>();
        lines.add(DetailLine.header("Validation Results"));
        lines.add(DetailLine.blank());

        int ok = 0, warn = 0, err = 0;
        for (ZaubereiReloadListener.ValidationResult result : validationResults) {
            int color;
            String icon;
            switch (result.status()) {
                case OK:
                    color = COLOR_SPECIFIC;
                    icon = "\u2714 "; // ✔
                    ok++;
                    break;
                case WARNING:
                    color = COLOR_UNIVERSAL;
                    icon = "\u26A0 "; // ⚠
                    warn++;
                    break;
                default:
                    color = 0xFFFF5555;
                    icon = "\u2718 "; // ✘
                    err++;
                    break;
            }

            lines.add(DetailLine.text(icon + result.filePath(), color));
            if (result.status() != ZaubereiReloadListener.ValidationResult.Status.OK) {
                // Wrap long messages
                String msg = result.message();
                List<String> wrapped = wrapText(msg, rightW - 20);
                for (String line : wrapped) {
                    lines.add(DetailLine.indented(line, COLOR_GRAY));
                }
            }
        }

        lines.add(DetailLine.blank());
        lines.add(DetailLine.header("Summary: " + ok + " OK, " + warn + " Warning(s), " + err + " Error(s)"));

        // Render with scroll
        int y = rightY - rightScrollOffset * lineHeight;
        for (DetailLine line : lines) {
            if (y >= rightY - lineHeight && y < rightY + rightH) {
                if (line.bold) {
                    graphics.drawString(this.font,
                            Component.literal(line.text).withStyle(s -> s.withBold(true)),
                            rightX + line.indent + 3, y, line.color, false);
                } else {
                    graphics.drawString(this.font, line.text,
                            rightX + line.indent + 3, y, line.color, false);
                }
            }
            y += lineHeight;
        }

        // Scrollbar
        if (lines.size() > maxLinesRight) {
            int scrollBarX = rightX + rightW - 3;
            float ratio = (float) rightScrollOffset / Math.max(1, lines.size() - maxLinesRight);
            int thumbHeight = Math.max(10, rightH * maxLinesRight / lines.size());
            int thumbY = rightY + (int) ((rightH - thumbHeight) * ratio);

            graphics.fill(scrollBarX, rightY, scrollBarX + 2, rightY + rightH, 0x33000000);
            graphics.fill(scrollBarX, thumbY, scrollBarX + 2, thumbY + thumbHeight, 0xAA553300);
        }

        graphics.disableScissor();
    }

    // ─── Input handling ──────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        // Click on left page → select entry
        if (mouseX >= leftX && mouseX <= leftX + leftW
                && mouseY >= leftY && mouseY <= leftY + leftH) {

            int clickedLine = (int) ((mouseY - leftY) / lineHeight) + leftScrollOffset;
            if (clickedLine >= 0 && clickedLine < listEntries.size()) {
                ListEntry entry = listEntries.get(clickedLine);
                if (entry.type() == ListEntry.EntryType.SCOPE_ENTRY) {
                    selectedIndex = clickedLine;
                    rightScrollOffset = 0;
                    showingValidation = false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll left page
        if (mouseX >= leftX && mouseX <= leftX + leftW
                && mouseY >= leftY && mouseY <= leftY + leftH) {
            if (scrollY > 0) {
                leftScrollOffset = Math.max(0, leftScrollOffset - 1);
            } else if (scrollY < 0) {
                leftScrollOffset = Math.min(
                        Math.max(0, listEntries.size() - maxLinesLeft),
                        leftScrollOffset + 1);
            }
            return true;
        }

        // Scroll right page
        if (mouseX >= rightX && mouseX <= rightX + rightW
                && mouseY >= rightY && mouseY <= rightY + rightH) {
            if (scrollY > 0) {
                rightScrollOffset = Math.max(0, rightScrollOffset - 1);
            } else if (scrollY < 0) {
                rightScrollOffset = rightScrollOffset + 1;
                // Max will be clamped naturally in render (no content = no visual)
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Arrow keys for list navigation
        if (keyCode == 265) { // UP
            navigateList(-1);
            return true;
        }
        if (keyCode == 264) { // DOWN
            navigateList(1);
            return true;
        }

        // ESC to close
        if (keyCode == 256) {
            onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void navigateList(int direction) {
        if (listEntries.isEmpty()) return;

        int newIndex = selectedIndex;
        do {
            newIndex += direction;
            if (newIndex < 0) newIndex = listEntries.size() - 1;
            if (newIndex >= listEntries.size()) newIndex = 0;
            // Skip headers
            if (listEntries.get(newIndex).type() == ListEntry.EntryType.SCOPE_ENTRY) {
                selectedIndex = newIndex;
                rightScrollOffset = 0;
                showingValidation = false;

                // Auto-scroll left list to keep selection visible
                if (selectedIndex < leftScrollOffset) {
                    leftScrollOffset = selectedIndex;
                } else if (selectedIndex >= leftScrollOffset + maxLinesLeft) {
                    leftScrollOffset = selectedIndex - maxLinesLeft + 1;
                }
                return;
            }
        } while (newIndex != selectedIndex); // prevent infinite loop if all headers
    }

    // ─── Button actions ──────────────────────────────────────────────────

    private void onReload() {
        // Reload set definitions from config files
        ZaubereiReloadListener.loadAllEffects();
        buildListEntries();
    }

    private void onValidate() {
        validationResults = ZaubereiReloadListener.validateAllFiles();
        showingValidation = true;
        rightScrollOffset = 0;
    }

    // ─── Helper methods ──────────────────────────────────────────────────

    private String resolveEffectName(String effectId) {
        try {
            ResourceLocation loc = ResourceLocation.parse(
                    effectId.contains(":") ? effectId : "minecraft:" + effectId);
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(loc);
            if (effect != null) {
                return effect.getDisplayName().getString();
            }
        } catch (Exception ignored) {}
        return effectId;
    }

    private String resolveAttributeName(String attributeId) {
        try {
            ResourceLocation loc = ResourceLocation.parse(
                    attributeId.contains(":") ? attributeId : "minecraft:" + attributeId);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(loc);
            if (attr != null) {
                return Component.translatable(attr.getDescriptionId()).getString();
            }
        } catch (Exception ignored) {}
        // Fallback: format the path nicely
        String path = attributeId.contains(":") ? attributeId.split(":")[1] : attributeId;
        return path.replace("generic.", "").replace("_", " ");
    }

    private String formatAttributeValue(ArmorSetData.AttributeData data) {
        double val = data.getValue();
        String mod = data.getModifier();
        if (mod != null && (mod.equalsIgnoreCase("multiply")
                || mod.equalsIgnoreCase("multiply_base")
                || mod.equalsIgnoreCase("multiply_total"))) {
            return String.format("+%.0f%%", val * 100);
        }
        if (val == (long) val) {
            return String.format("+%d", (long) val);
        }
        return String.format("+%.2f", val);
    }

    private static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() > 0
                    && this.font.width(current.toString() + " " + word) > maxWidth) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }

        return lines;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}