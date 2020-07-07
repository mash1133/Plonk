package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.item.ItemBlockPlacedItems;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

public class RegistryItems {
    public static final ItemBlockPlacedItems placed_items = new ItemBlockPlacedItems(new Item.Properties());
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegistryEvent.Register<Item> event) {
        for (Field f : RegistryItems.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Item.class.isAssignableFrom(f.getType())) {
                        ResourceLocation rl = new ResourceLocation(Plonk.MODID, f.getName());
                        LOG.info(REGISTRIES, "Registering Item: " + rl);
                        Item item = (Item) f.get(null);
                        item.setRegistryName(rl);
                        event.getRegistry().register(item);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
