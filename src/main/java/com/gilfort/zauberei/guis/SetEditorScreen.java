package com.gilfort.zauberei.guis;

import com.gilfort.zauberei.item.armorbonus.ArmorSetData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData.AttributeData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData.EffectData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData.PartData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * SetEditorScreen — Edit or create an ArmorSet JSON.
 *
 * <p>Layout:
 * <ul>
 *   <li>Top: Set name display + Part-tabs (1Part … 4Part)</li>
 *   <li>Left panel: Effects list for the active part</li>
 *   <li>Right panel: Attributes list for the active part</li>
 *   <li>Bottom: Save + Cancel buttons</li>
 * </ul>
 *
 * <p>Saves directly to {@code config/zauberei/set-effects/<setId>.json}.
 *
 * @see ArmorSetData
 * @see SetsManagerScreen
 */
@OnlyIn(Dist.CLIENT)
public class SetEditorScreen extends Screen {

    // ──── Color Scheme (consistent with SetsManagerScreen) ──────────────
    private static final int PANEL_BG       = 0xFFF5F0E0;
    private static final int PANEL_BORDER   = 0xFF8B7355;
    private static final int COLOR_TEXT     = 0xFF000000;
    private static final int COLOR_GRAY     = 0xFF666666;
    private static final int COLOR_SELECTED = 0x44FFCC00;
    private static final int COLOR_HOVER    = 0x22FFCC00;
    private static final int TAB_ACTIVE_BG  = 0xFFDDD0A0;
    private static final int TAB_INACTIVE_BG= 0xFFBBAA88;
    private static final int COLOR_RED_BTN  = 0xFFCC3333;
    private static final int COLOR_GREEN    = 0xFF226622;

    // ──── Layout ────────────────────────────────────────────────────────
    private static final int PADDING        = 8;
    private static final int TAB_HEIGHT     = 20;
    private static final int TAB_WIDTH      = 60;
    private static final int ITEM_HEIGHT    = 14;
    private static final int SECTION_TITLE_H= 12;
    private static final int PARTS          = 4; // 1Part … 4Part

    // ──── Save path ─────────────────────────────────────────────────────
    private static final Path SAVE_DIR = Path.of("config", "zauberei", "set-effects");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ──── Data ──────────────────────────────────────────────────────────
    private final Screen parentScreen;
    private final String setId;           // filename without .json
    private final String displayName;     // shown in title

    /**
     * Working copy of the set data — one PartData per part index (0=1Part … 3=4Part).
     * We use ArrayList<EffectData> and LinkedHashMap<String,AttributeData> so order is stable.
     */
    private final List<EffectData>[]              partEffects;
    private final Map<String, AttributeData>[]    partAttributes;

    // ──── UI State ───────────────────────────────────────────────────────
    private int activeTab = 0;  // 0 = 1Part, 1 = 2Part, …
    private int effectScrollOffset   = 0;
    private int attributeScrollOffset= 0;
    private int selectedEffectIndex   = -1;
    private int selectedAttributeIndex= -1;

    // ──── Derived layout (computed in init) ─────────────────────────────
    private int contentTop;   // y below tabs
    private int contentBottom;// y above buttons
    private int leftPanelX, leftPanelW;
    private int rightPanelX, rightPanelW;
    private int panelListTop, panelListH;
    private int maxEffectVisible, maxAttrVisible;

    // ──── Status message ────────────────────────────────────────────────
    private String statusMessage = "";
    private boolean statusIsError = false;

    // ════════════════════════════════════════════════════════════════════
    //  Constructors
    // ════════════════════════════════════════════════════════════════════

    /**
     * Create-mode: starts with empty data.
     */
    @SuppressWarnings("unchecked")
    public SetEditorScreen(Screen parent, String setId, String displayName) {
        super(Component.literal("Edit Set: " + displayName));
        this.parentScreen = parent;
        this.setId        = setId;
        this.displayName  = displayName;
        this.partEffects    = new ArrayList[PARTS];
        this.partAttributes = new LinkedHashMap[PARTS];
        for (int i = 0; i < PARTS; i++) {
            partEffects[i]    = new ArrayList<>();
            partAttributes[i] = new LinkedHashMap<>();
        }
    }

    /**
     * Edit-mode: pre-loads existing {@link ArmorSetData}.
     */
    @SuppressWarnings("unchecked")
    public SetEditorScreen(Screen parent, String setId, ArmorSetData existing) {
        super(Component.literal("Edit Set: " +
                (existing.getDisplayName() != null ? existing.getDisplayName() : setId)));
        this.parentScreen = parent;
        this.setId        = setId;
        this.displayName  = existing.getDisplayName() != null ? existing.getDisplayName() : setId;
        this.partEffects    = new ArrayList[PARTS];
        this.partAttributes = new LinkedHashMap[PARTS];

        for (int i = 0; i < PARTS; i++) {
            partEffects[i]    = new ArrayList<>();
            partAttributes[i] = new LinkedHashMap<>();
        }

        // Load existing data into working copies
        if (existing.getParts() != null) {
            for (int i = 0; i < PARTS; i++) {
                String key = (i + 1) + "Part";
                PartData pd = existing.getParts().get(key);
                if (pd != null) {
                    if (pd.getEffects() != null) {
                        partEffects[i].addAll(pd.getEffects());
                    }
                    if (pd.getAttributes() != null) {
                        partAttributes[i].putAll(pd.getAttributes());
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Init
    // ════════════════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();

        int tabsTop    = PADDING + 14; // below title
        contentTop     = tabsTop + TAB_HEIGHT + 4;
        int btnH       = 20;
        contentBottom  = this.height - PADDING - btnH - 4;

        // Two panels side by side
        int totalW     = this.width - 2 * PADDING;
        leftPanelX     = PADDING;
        leftPanelW     = (totalW - PADDING) / 2;
        rightPanelX    = leftPanelX + leftPanelW + PADDING;
        rightPanelW    = totalW - leftPanelW - PADDING;

        // List area inside each panel (below section title + add button)
        panelListTop   = contentTop + PADDING + SECTION_TITLE_H + 4 + 20 + 4;
        panelListH     = contentBottom - panelListTop - PADDING;
        maxEffectVisible = panelListH / ITEM_HEIGHT;
        maxAttrVisible   = panelListH / ITEM_HEIGHT;

        // ── Buttons ──────────────────────────────────────────────────────
        int btnY = contentBottom + 4;

        // Add Effect
        addRenderableWidget(Button.builder(
                        Component.literal("+ Effect"), btn -> openAddEffectPopup())
                .bounds(leftPanelX, contentTop + PADDING + SECTION_TITLE_H + 4, leftPanelW / 2 - 2, 18)
                .build());

        // Remove Effect
        addRenderableWidget(Button.builder(
                        Component.literal("- Remove"), btn -> removeSelectedEffect())
                .bounds(leftPanelX + leftPanelW / 2 + 2, contentTop + PADDING + SECTION_TITLE_H + 4, leftPanelW / 2 - 2, 18)
                .build());

        // Add Attribute
        addRenderableWidget(Button.builder(
                        Component.literal("+ Attribute"), btn -> openAddAttributePopup())
                .bounds(rightPanelX, contentTop + PADDING + SECTION_TITLE_H + 4, rightPanelW / 2 - 2, 18)
                .build());

        // Remove Attribute
        addRenderableWidget(Button.builder(
                        Component.literal("- Remove"), btn -> removeSelectedAttribute())
                .bounds(rightPanelX + rightPanelW / 2 + 2, contentTop + PADDING + SECTION_TITLE_H + 4, rightPanelW / 2 - 2, 18)
                .build());

        // Save
        addRenderableWidget(Button.builder(
                        Component.literal("💾 Save"), btn -> saveToJson())
                .bounds(this.width / 2 - 84, btnY, 80, btnH)
                .build());

        // Cancel
        addRenderableWidget(Button.builder(
                        Component.literal("Cancel"), btn -> onClose())
                .bounds(this.width / 2 + 4, btnY, 80, btnH)
                .build());
    }

    // ════════════════════════════════════════════════════════════════════
    //  Rendering
    // ════════════════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        g.fill(0, 0, this.width, this.height, 0xFF2A2A2A);

        // Title
        g.drawString(this.font,
                Component.literal("✦ Set Editor: ").withStyle(ChatFormatting.BOLD)
                        .append(Component.literal(displayName)),
                PADDING, PADDING, 0xFFFFDD88, false);

        // Tabs
        renderTabs(g, mouseX, mouseY);

        // Panels
        renderPanel(g, leftPanelX,  contentTop, leftPanelW,  contentBottom - contentTop, "Effects",    true,  mouseX, mouseY);
        renderPanel(g, rightPanelX, contentTop, rightPanelW, contentBottom - contentTop, "Attributes", false, mouseX, mouseY);

        // Widgets (buttons)
        super.render(g, mouseX, mouseY, partialTick);

        // Status message
        if (!statusMessage.isEmpty()) {
            int color = statusIsError ? 0xFFFF4444 : 0xFF44CC44;
            g.drawString(this.font, statusMessage,
                    this.width / 2 - this.font.width(statusMessage) / 2,
                    contentBottom + 28, color, false);
        }
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        int tabsTop = PADDING + 14;
        int tabsStartX = PADDING;

        for (int i = 0; i < PARTS; i++) {
            int tx = tabsStartX + i * (TAB_WIDTH + 2);
            int ty = tabsTop;
            boolean active = (i == activeTab);
            int bg = active ? TAB_ACTIVE_BG : TAB_INACTIVE_BG;

            g.fill(tx, ty, tx + TAB_WIDTH, ty + TAB_HEIGHT, bg);
            // Border
            g.fill(tx, ty, tx + TAB_WIDTH, ty + 1, PANEL_BORDER);
            g.fill(tx, ty, tx + 1, ty + TAB_HEIGHT, PANEL_BORDER);
            g.fill(tx + TAB_WIDTH - 1, ty, tx + TAB_WIDTH, ty + TAB_HEIGHT, PANEL_BORDER);
            if (!active) g.fill(tx, ty + TAB_HEIGHT - 1, tx + TAB_WIDTH, ty + TAB_HEIGHT, PANEL_BORDER);

            String label = (i + 1) + " Part" + (i + 1 > 1 ? "s" : "");
            int textX = tx + (TAB_WIDTH - this.font.width(label)) / 2;
            int textY = ty + (TAB_HEIGHT - 8) / 2;
            g.drawString(this.font, label, textX, textY, active ? COLOR_TEXT : 0xFF888877, false);

            // Badge: count of effects+attributes
            int count = partEffects[i].size() + partAttributes[i].size();
            if (count > 0) {
                String badge = String.valueOf(count);
                int bx = tx + TAB_WIDTH - this.font.width(badge) - 3;
                g.drawString(this.font, badge, bx, ty + 2, 0xFFFFAA00, false);
            }
        }
    }

    private void renderPanel(GuiGraphics g, int px, int py, int pw, int ph,
                             String title, boolean isEffects, int mouseX, int mouseY) {
        // Panel background + border
        g.fill(px, py, px + pw, py + ph, PANEL_BG);
        g.fill(px,          py,          px + pw,     py + 1,      PANEL_BORDER);
        g.fill(px,          py + ph - 1, px + pw,     py + ph,     PANEL_BORDER);
        g.fill(px,          py,          px + 1,       py + ph,     PANEL_BORDER);
        g.fill(px + pw - 1, py,          px + pw,     py + ph,     PANEL_BORDER);

        // Section title
        g.drawString(this.font,
                Component.literal(title).withStyle(ChatFormatting.BOLD),
                px + PADDING, py + PADDING, COLOR_TEXT, false);

        // List entries
        if (isEffects) {
            renderEffectList(g, mouseX, mouseY);
        } else {
            renderAttributeList(g, mouseX, mouseY);
        }
    }

    private void renderEffectList(GuiGraphics g, int mouseX, int mouseY) {
        List<EffectData> effects = partEffects[activeTab];
        int lx = leftPanelX + PADDING;
        int ly = panelListTop;
        int lw = leftPanelW - 2 * PADDING;

        g.enableScissor(lx, ly, lx + lw, ly + panelListH);

        if (effects.isEmpty()) {
            g.drawString(this.font,
                    Component.literal("No effects.").withStyle(ChatFormatting.ITALIC),
                    lx + 2, ly + 3, COLOR_GRAY, false);
        } else {
            int visEnd = Math.min(effects.size(), effectScrollOffset + maxEffectVisible);
            for (int i = effectScrollOffset; i < visEnd; i++) {
                int ey = ly + (i - effectScrollOffset) * ITEM_HEIGHT;
                EffectData ed = effects.get(i);

                if (i == selectedEffectIndex) {
                    g.fill(lx, ey, lx + lw, ey + ITEM_HEIGHT, COLOR_SELECTED);
                } else if (mouseX >= lx && mouseX <= lx + lw && mouseY >= ey && mouseY < ey + ITEM_HEIGHT) {
                    g.fill(lx, ey, lx + lw, ey + ITEM_HEIGHT, COLOR_HOVER);
                }

                // Format: "minecraft:speed  Amp: 1"
                String effectName = shortName(ed.getEffect());
                String ampStr = "Amp: " + ed.getAmplifier();
                g.drawString(this.font, effectName, lx + 3, ey + 3, COLOR_TEXT, false);
                int ampX = lx + lw - this.font.width(ampStr) - 3;
                g.drawString(this.font, ampStr, ampX, ey + 3, COLOR_GRAY, false);
            }
        }

        g.disableScissor();
    }

    private void renderAttributeList(GuiGraphics g, int mouseX, int mouseY) {
        Map<String, AttributeData> attrs = partAttributes[activeTab];
        List<Map.Entry<String, AttributeData>> entries = new ArrayList<>(attrs.entrySet());
        int lx = rightPanelX + PADDING;
        int ly = panelListTop;
        int lw = rightPanelW - 2 * PADDING;

        g.enableScissor(lx, ly, lx + lw, ly + panelListH);

        if (entries.isEmpty()) {
            g.drawString(this.font,
                    Component.literal("No attributes.").withStyle(ChatFormatting.ITALIC),
                    lx + 2, ly + 3, COLOR_GRAY, false);
        } else {
            int visEnd = Math.min(entries.size(), attributeScrollOffset + maxAttrVisible);
            for (int i = attributeScrollOffset; i < visEnd; i++) {
                int ay = ly + (i - attributeScrollOffset) * ITEM_HEIGHT;
                Map.Entry<String, AttributeData> entry = entries.get(i);
                AttributeData ad = entry.getValue();

                if (i == selectedAttributeIndex) {
                    g.fill(lx, ay, lx + lw, ay + ITEM_HEIGHT, COLOR_SELECTED);
                } else if (mouseX >= lx && mouseX <= lx + lw && mouseY >= ay && mouseY < ay + ITEM_HEIGHT) {
                    g.fill(lx, ay, lx + lw, ay + ITEM_HEIGHT, COLOR_HOVER);
                }

                // Format: "generic.max_health  +5.0 (add)"
                String attrName = shortName(entry.getKey());
                String valStr = formatAttrValue(ad);
                g.drawString(this.font, attrName, lx + 3, ay + 3, COLOR_TEXT, false);
                int valX = lx + lw - this.font.width(valStr) - 3;
                g.drawString(this.font, valStr, valX, ay + 3, COLOR_GRAY, false);
            }
        }

        g.disableScissor();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Input
    // ════════════════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Tab clicks
        int tabsTop = PADDING + 14;
        for (int i = 0; i < PARTS; i++) {
            int tx = PADDING + i * (TAB_WIDTH + 2);
            if (mouseX >= tx && mouseX <= tx + TAB_WIDTH
                    && mouseY >= tabsTop && mouseY <= tabsTop + TAB_HEIGHT) {
                activeTab = i;
                selectedEffectIndex    = -1;
                selectedAttributeIndex = -1;
                effectScrollOffset     = 0;
                attributeScrollOffset  = 0;
                return true;
            }
        }

        // Effect list click
        int lx = leftPanelX + PADDING;
        int lw = leftPanelW - 2 * PADDING;
        if (mouseX >= lx && mouseX <= lx + lw
                && mouseY >= panelListTop && mouseY <= panelListTop + panelListH) {
            int idx = (int) ((mouseY - panelListTop) / ITEM_HEIGHT) + effectScrollOffset;
            if (idx >= 0 && idx < partEffects[activeTab].size()) {
                selectedEffectIndex    = idx;
                selectedAttributeIndex = -1;
            }
            return true;
        }

        // Attribute list click
        int rx = rightPanelX + PADDING;
        int rw = rightPanelW - 2 * PADDING;
        if (mouseX >= rx && mouseX <= rx + rw
                && mouseY >= panelListTop && mouseY <= panelListTop + panelListH) {
            List<Map.Entry<String, AttributeData>> entries =
                    new ArrayList<>(partAttributes[activeTab].entrySet());
            int idx = (int) ((mouseY - panelListTop) / ITEM_HEIGHT) + attributeScrollOffset;
            if (idx >= 0 && idx < entries.size()) {
                selectedAttributeIndex = idx;
                selectedEffectIndex    = -1;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int lx = leftPanelX + PADDING;
        int lw = leftPanelW - 2 * PADDING;
        if (mouseX >= lx && mouseX <= lx + lw
                && mouseY >= panelListTop && mouseY <= panelListTop + panelListH) {
            if (scrollY > 0) effectScrollOffset = Math.max(0, effectScrollOffset - 1);
            else effectScrollOffset = Math.min(
                    Math.max(0, partEffects[activeTab].size() - maxEffectVisible),
                    effectScrollOffset + 1);
            return true;
        }
        int rx = rightPanelX + PADDING;
        int rw = rightPanelW - 2 * PADDING;
        if (mouseX >= rx && mouseX <= rx + rw
                && mouseY >= panelListTop && mouseY <= panelListTop + panelListH) {
            List<Map.Entry<String, AttributeData>> entries =
                    new ArrayList<>(partAttributes[activeTab].entrySet());
            if (scrollY > 0) attributeScrollOffset = Math.max(0, attributeScrollOffset - 1);
            else attributeScrollOffset = Math.min(
                    Math.max(0, entries.size() - maxAttrVisible),
                    attributeScrollOffset + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { onClose(); return true; } // Escape
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ════════════════════════════════════════════════════════════════════
    //  Popup flows — Add Effect
    // ════════════════════════════════════════════════════════════════════

    /**
     * Step 1: Pick a MobEffect via SearchableListPopup.
     * Step 2: Enter amplifier via a small inline EditBox popup.
     */
    private void openAddEffectPopup() {
        // Build effect entries from Minecraft's built-in registry
        List<SearchableListPopup.Entry<String>> effectEntries = new ArrayList<>();
        net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
            net.minecraft.resources.ResourceLocation rl =
                    net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (rl != null) {
                effectEntries.add(new SearchableListPopup.Entry<>(
                        rl.toString(),
                        Component.literal(rl.getPath().replace("_", " ")),
                        rl.toString()));
            }
        });
        effectEntries.sort(Comparator.comparing(e -> e.displayName().getString()));

        this.minecraft.setScreen(new SearchableListPopup<>(
                Component.literal("Select Effect"),
                this, effectEntries,
                effectId -> openAmplifierPopup(effectId)));
    }

    /**
     * Step 2: Ask for amplifier value (0–9).
     */
    private void openAmplifierPopup(String effectId) {
        this.minecraft.setScreen(new AmplifierInputScreen(this, effectId, amplifier -> {
            EffectData ed = new EffectData();
            ed.setEffect(effectId);
            ed.setAmplifier(amplifier);
            partEffects[activeTab].add(ed);
            setStatus("Effect added!", false);
        }));
    }

    // ════════════════════════════════════════════════════════════════════
    //  Popup flows — Add Attribute
    // ════════════════════════════════════════════════════════════════════

    private void openAddAttributePopup() {
        List<SearchableListPopup.Entry<String>> attrEntries = new ArrayList<>();
        net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.forEach(attr -> {
            net.minecraft.resources.ResourceLocation rl =
                    net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(attr);
            if (rl != null) {
                attrEntries.add(new SearchableListPopup.Entry<>(
                        rl.toString(),
                        Component.literal(rl.getPath().replace("_", " ")),
                        rl.toString()));
            }
        });
        attrEntries.sort(Comparator.comparing(e -> e.displayName().getString()));

        this.minecraft.setScreen(new SearchableListPopup<>(
                Component.literal("Select Attribute"),
                this, attrEntries,
                attrId -> openAttributeValuePopup(attrId)));
    }

    private void openAttributeValuePopup(String attrId) {
        this.minecraft.setScreen(new AttributeValueScreen(this, attrId, (value, modifier) -> {
            AttributeData ad = new AttributeData();
            ad.setValue(value);
            ad.setModifier(modifier);
            partAttributes[activeTab].put(attrId, ad);
            setStatus("Attribute added!", false);
        }));
    }

    // ════════════════════════════════════════════════════════════════════
    //  Remove
    // ════════════════════════════════════════════════════════════════════

    private void removeSelectedEffect() {
        if (selectedEffectIndex >= 0 && selectedEffectIndex < partEffects[activeTab].size()) {
            partEffects[activeTab].remove(selectedEffectIndex);
            selectedEffectIndex = -1;
            setStatus("Effect removed.", false);
        }
    }

    private void removeSelectedAttribute() {
        List<String> keys = new ArrayList<>(partAttributes[activeTab].keySet());
        if (selectedAttributeIndex >= 0 && selectedAttributeIndex < keys.size()) {
            partAttributes[activeTab].remove(keys.get(selectedAttributeIndex));
            selectedAttributeIndex = -1;
            setStatus("Attribute removed.", false);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Save to JSON
    // ════════════════════════════════════════════════════════════════════

    private void saveToJson() {
        try {
            // Build ArmorSetData from working copies
            ArmorSetData data = new ArmorSetData();
            data.setDisplayName(displayName);

            Map<String, PartData> partsMap = new LinkedHashMap<>();
            for (int i = 0; i < PARTS; i++) {
                List<EffectData> effects = partEffects[i];
                Map<String, AttributeData> attrs = partAttributes[i];

                // Only write parts that have at least one effect or attribute
                if (!effects.isEmpty() || !attrs.isEmpty()) {
                    PartData pd = new PartData();
                    pd.setEffects(new ArrayList<>(effects));
                    pd.setAttributes(new LinkedHashMap<>(attrs));
                    partsMap.put((i + 1) + "Part", pd);
                }
            }
            data.setParts(partsMap);

            // Ensure directory exists
            Files.createDirectories(SAVE_DIR);

            // Write JSON
            Path file = SAVE_DIR.resolve(setId + ".json");
            String json = GSON.toJson(data);
            Files.writeString(file, json);

            setStatus("✔ Saved to " + file, false);
        } catch (IOException e) {
            setStatus("✘ Save failed: " + e.getMessage(), true);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════

    private void setStatus(String msg, boolean isError) {
        this.statusMessage = msg;
        this.statusIsError = isError;
    }

    /** Shortens "minecraft:generic_max_health" → "generic_max_health" */
    private String shortName(String resourceLocation) {
        if (resourceLocation == null) return "?";
        int colon = resourceLocation.indexOf(':');
        return colon >= 0 ? resourceLocation.substring(colon + 1) : resourceLocation;
    }

    private String formatAttrValue(AttributeData ad) {
        String mod = ad.getModifier() != null ? ad.getModifier() : "add";
        String sign = ad.getValue() >= 0 ? "+" : "";
        return sign + String.format("%.2f", ad.getValue()) + " (" + mod + ")";
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
