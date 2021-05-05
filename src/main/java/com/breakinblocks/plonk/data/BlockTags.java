package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class BlockTags extends BlockTagsProvider {
    public BlockTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn, Plonk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
    }

    @Override
    public String getName() {
        return Plonk.NAME + " Block Tags";
    }
}
