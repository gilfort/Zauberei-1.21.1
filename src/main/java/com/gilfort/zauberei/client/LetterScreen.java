package com.gilfort.zauberei.client;

import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import com.gilfort.zauberei.network.LetterButtonPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Screen of the Introduction Letter: shows the letter text and a button to tear the letter,
 * which asks the server for the starting items.
 */
public class LetterScreen extends Screen {

    private static final ResourceLocation BACKGROUND = Zauberei.id("textures/gui/letter.png");
    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;

    private static final int BUTTON_X = 88;
    private static final int BUTTON_Y = 220;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;

    private static final int TEXT_X = 20;
    private static final int TEXT_Y = 40;
    private static final int MAX_TEXT_WIDTH = 230;
    private static final int MAX_TEXT_HEIGHT = 170;

    private Component letterText = Component.empty();

    /** Creates the screen. */
    public LetterScreen() {
        super(Component.translatable(ZaubereiItems.INTRODUCTIONLETTER.get().getDescriptionId()));
    }

    @Override
    protected void init() {
        super.init();
        this.letterText = buildLetterText();

        int guiLeft = (this.width - IMAGE_WIDTH) / 2;
        int guiTop = (this.height - IMAGE_HEIGHT) / 2;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.zauberei.introduction_letter.tear"),
                                button -> {
                                    PacketDistributor.sendToServer(new LetterButtonPayload());
                                    this.onClose();
                                })
                        .bounds(guiLeft + BUTTON_X, guiTop + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build()
        );
    }

    /**
     * Uses the config override if set, otherwise the translated default text.
     * Called from {@link #init()}, where {@code minecraft} is available.
     */
    private Component buildLetterText() {
        String playerName = this.minecraft != null && this.minecraft.player != null
                ? this.minecraft.player.getDisplayName().getString()
                : "";
        String override = Config.LETTER_TEXT.get();
        if (!override.isBlank()) {
            return Component.literal(override.replace("%player%", playerName).replace("\r", ""));
        }
        return Component.translatable("message.zauberei.introduction_letter.text", playerName);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int guiLeft = (this.width - IMAGE_WIDTH) / 2;
        int guiTop = (this.height - IMAGE_HEIGHT) / 2;

        // Draw the background slightly behind the widgets.
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, -1);
        guiGraphics.blit(BACKGROUND, guiLeft, guiTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
        guiGraphics.pose().popPose();

        List<FormattedCharSequence> wrappedText = this.font.split(letterText, MAX_TEXT_WIDTH);
        int lineHeight = this.font.lineHeight;
        int textHeight = lineHeight * wrappedText.size();

        // Shrink long texts so they fit into the letter.
        float scale = textHeight > MAX_TEXT_HEIGHT ? (float) MAX_TEXT_HEIGHT / textHeight : 1.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.pose().translate((guiLeft + TEXT_X) / scale, (guiTop + TEXT_Y) / scale, 0);
        for (int i = 0; i < wrappedText.size(); i++) {
            guiGraphics.drawString(this.font, wrappedText.get(i), 0, lineHeight * i, 0x000000, false);
        }
        guiGraphics.pose().popPose();
    }
}
