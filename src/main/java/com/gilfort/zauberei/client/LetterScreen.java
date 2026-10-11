package com.gilfort.zauberei.client;

import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import com.gilfort.zauberei.network.LetterButtonPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

/**
 * Screen of the Introduction Letter: shows the letter text and a button to tear the letter,
 * which asks the server for the starting items.
 */
public class LetterScreen extends Screen {

    private static final Identifier BACKGROUND = Zauberei.id("textures/gui/letter.png");
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
    private static final int TEXT_COLOR = 0xFF000000;

    private Component letterText = Component.empty();

    /** Creates the screen. */
    public LetterScreen() {
        super(Component.translatable(ZaubereiItems.INTRODUCTIONLETTER.get().getDescriptionId()));
    }

    @Override
    protected void init() {
        super.init();
        this.letterText = buildLetterText();

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.zauberei.introduction_letter.tear"),
                                button -> {
                                    ClientPacketDistributor.sendToServer(new LetterButtonPayload());
                                    this.onClose();
                                })
                        .bounds(guiLeft() + BUTTON_X, guiTop() + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build()
        );
    }

    /**
     * Uses the config override if set, otherwise the translated default text.
     * Called from {@link #init()}, where {@code minecraft} is available.
     */
    private Component buildLetterText() {
        String playerName = this.minecraft.player != null ? this.minecraft.player.getPlainTextName() : "";
        String override = Config.LETTER_TEXT.get();
        if (!override.isBlank()) {
            return Component.literal(override.replace("%player%", playerName).replace("\r", ""));
        }
        return Component.translatable("message.zauberei.introduction_letter.text", playerName);
    }

    private int guiLeft() {
        return (this.width - IMAGE_WIDTH) / 2;
    }

    private int guiTop() {
        return (this.height - IMAGE_HEIGHT) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft(), guiTop(), 0, 0,
                IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        List<FormattedCharSequence> wrappedText = this.font.split(letterText, MAX_TEXT_WIDTH);
        int lineHeight = this.font.lineHeight;
        int textHeight = lineHeight * wrappedText.size();

        // Shrink long texts so they fit into the letter.
        float scale = textHeight > MAX_TEXT_HEIGHT ? (float) MAX_TEXT_HEIGHT / textHeight : 1.0f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(guiLeft() + TEXT_X, guiTop() + TEXT_Y);
        graphics.pose().scale(scale, scale);
        for (int i = 0; i < wrappedText.size(); i++) {
            graphics.text(this.font, wrappedText.get(i), 0, lineHeight * i, TEXT_COLOR, false);
        }
        graphics.pose().popMatrix();
    }
}
