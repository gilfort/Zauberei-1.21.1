package com.gilfort.zauberei.guis;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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

    String invitation = String.format("Dear %s, \n" +
            "We are pleased to inform you that our potential detection system, \"Oculus,\" has recognized your magical talent.\n" +
            "It is our honor to invite you to join our school " +
            "as a scholarship student.\n" +
            "From today on you are part of the Witchwood Academy. \n" +
            "To receive your first-year supplies, please tear this letter apart. \n" +
            "Welcome to a world where magic comes to life. " +
            "We look forward to guiding you on your journey " +
            "and helping you master your new skills.\n\n" +
            "Yours sincerely,\n" +
            "Professor McDumblesnape\n" +
            "Headmaster of Witchwood Academy", playername);


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
                                        if (minecraft != null && minecraft.player != null) {
                                            minecraft.player.getInventory().removeItem(letterItem);

                                            ItemStack[] armorPieces = {
                                                    ZaubereiItems.MAGICCLOTH_HELMET.get().getDefaultInstance(),
                                                    ZaubereiItems.MAGICCLOTH_CHESTPLATE.get().getDefaultInstance(),
                                                    ZaubereiItems.MAGICCLOTH_LEGGINGS.get().getDefaultInstance(),
                                                    ZaubereiItems.MAGICCLOTH_BOOTS.get().getDefaultInstance()
                                            };

                                            for(ItemStack armor : armorPieces){
                                                minecraft.player.getInventory().add(armor);
                                            }

                                            minecraft.player.playSound(SoundEvents.ITEM_PICKUP);

                                        }
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

            int guiLeft = (this.width - IMAGE_WIDTH) / 2;
            int guiTop = (this.height - IMAGE_HEIGHT) / 2;

            int TextBoxX = guiLeft + 20;
            int TextBoxY = guiTop + 20;
            int maxTextWidth = 190;
            int maxLines = 22;

            List<FormattedCharSequence> wrappedText = this.font.split(Component.literal(invitation), maxTextWidth);

            if(wrappedText.size() > maxLines) {
                wrappedText = wrappedText.subList(0, maxLines);
            }

            int lineHeight = this.font.lineHeight;
            for (int i = 0; i < wrappedText.size(); i++) {
                guiGraphics.drawString(
                        this.font,
                        wrappedText.get(i),
                        TextBoxX,
                        TextBoxY + (lineHeight * i),
                        0x000000,
                        false
                );
            }

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


        }
    }