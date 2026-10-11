package com.gilfort.zauberei.datagen;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

/**
 * Generates flat item models and client item definitions for every item in {@link ZaubereiItems}.
 * Worn armor is rendered by GeckoLib, so the armor items only need their inventory icon.
 */
public class ZaubereiModelProvider extends ModelProvider {

    /**
     * Creates the provider.
     *
     * @param output the pack output
     */
    public ZaubereiModelProvider(PackOutput output) {
        super(output, Zauberei.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        ZaubereiItems.ITEMS.getEntries().forEach(item -> itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM));
    }
}
