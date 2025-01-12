package com.gilfort.zauberei.datagen;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.util.ZaubereiTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ZaubereiItemTagProvider extends ItemTagsProvider {
    public ZaubereiItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Zauberei.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ZaubereiTags.Items.TRANSFORMABLE_ITEMS)
//                .add(ModItems.BISMUTH.get())
//                .add(ModItems.RAW_BISMUTH.get())
//                .add(Items.COAL)
//                .add(Items.STICK)
//                .add(Items.COMPASS);
        ;

    }
}