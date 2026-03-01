package com.gilfort.zauberei.guis;


import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.network.LetterButtonPayload;
import com.gilfort.zauberei.Zauberei;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class LetterGUI extends Screen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Zauberei.MODID, "textures/gui/letter.png");
    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;

    private static final int BUTTON_X = 88;
    private static final int BUTTON_Y = 220;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;

    String playername = getPlayerName();

    private String getPlayerName() {
        if (this.minecraft != null && this.minecraft.player != null) {
            return this.minecraft.player.getDisplayName().getString();
        }
        return "Trainee";
    }

    String invitation = Config.getLetterText(playername);


    private final ItemStack letterItem;

    public LetterGUI(ItemStack itemStack) {
            super(Component.literal("Introduction Letter"));
            this.letterItem = itemStack;
        }

        @Override
        protected void init() {
            super.init();

            int guiLeft = (this.width - IMAGE_WIDTH) / 2;
            int guiTop = (this.height - IMAGE_HEIGHT) / 2;

            this.addRenderableWidget(
                    Button.builder(
                                    Component.literal("RIP LETTER"),
                                    button -> {
                                        PacketDistributor.sendToServer(new LetterButtonPayload());
                                        minecraft.setScreen(null);
                                    }
                            )
                            .bounds(
                                    guiLeft + BUTTON_X,
                                    guiTop + BUTTON_Y,
                                    BUTTON_WIDTH,
                                    BUTTON_HEIGHT
                            )
                            .build()
            );
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);


//---render Background---

            int guiLeft = (this.width - IMAGE_WIDTH) / 2;
            int guiTop = (this.height - IMAGE_HEIGHT) / 2;



            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0,0,-1);

            guiGraphics.blit(
                    BACKGROUND,
                    guiLeft,
                    guiTop,
                    0,
                    0,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT
            );

            guiGraphics.pose().popPose();

    //---render Text---
            int TextBoxX = guiLeft + 20;
            int TextBoxY = guiTop + 40;
            int maxTextWidth = 230;
            int maxTextHeight = 170;

            List<FormattedCharSequence> wrappedText = this.font.split(Component.literal(invitation), maxTextWidth);

            int lineHeight = this.font.lineHeight;
            int textHeight = lineHeight * wrappedText.size();

            float scale = 1.0f;
            if(textHeight > maxTextHeight){
                scale = (float)maxTextHeight / textHeight;
            }

            float scaledX = TextBoxX / scale;
            float scaledY = TextBoxY / scale;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1.0f);
            guiGraphics.pose().translate(scaledX, scaledY, 0);

            for (int i=0; i < wrappedText.size(); i++){
                guiGraphics.drawString(
                        this.font,
                        wrappedText.get(i),
                        0,
                        (lineHeight * i),
                        0x000000,
                        false
                );
            }

            guiGraphics.pose().popPose();



        }
    }