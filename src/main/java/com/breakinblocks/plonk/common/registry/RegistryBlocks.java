package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

public class RegistryBlocks {
    public static final BlockPlacedItems placed_items = new BlockPlacedItems(AbstractBlock.Properties.create(RegistryMaterials.placed_items).sound(SoundType.STONE));
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegistryEvent.Register<Block> event) {
        for (Field f : RegistryBlocks.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Block.class.isAssignableFrom(f.getType())) {
                        ResourceLocation rl = new ResourceLocation(Plonk.MODID, f.getName());
                        LOG.info(REGISTRIES, "Registering Block: " + rl);
                        Block block = (Block) f.get(null);
                        block.setRegistryName(rl);
                        event.getRegistry().register(block);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
