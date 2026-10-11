package com.gilfort.zauberei.datagen;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates flat item models for every item in {@link ZaubereiItems}.
 * The Ars Nouveau variants use hand-written models in {@code src/main/resources}.
 */
public class ZaubereiItemModelProvider extends ItemModelProvider {

    /**
     * Creates the provider.
     *
     * @param output             the pack output
     * @param existingFileHelper helper to validate referenced textures
     */
    public ZaubereiItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Zauberei.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ZaubereiItems.ITEMS.getEntries().forEach(item -> basicItem(item.get()));
    }
}
