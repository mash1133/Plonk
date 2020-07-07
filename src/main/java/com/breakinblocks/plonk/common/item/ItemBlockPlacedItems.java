package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockPlacedItems extends BlockItem {
    private static final String TAG_HELD = "Held";
    private static final String TAG_RENDER_TYPE = TilePlacedItems.TAG_RENDER_TYPE;

    public ItemBlockPlacedItems(Item.Properties builder) {
        super(RegistryBlocks.placed_items, builder);
    }

    public void setHeldStack(ItemStack stack, ItemStack held, int renderType) {
        CompoundNBT tagCompound = stack.getOrCreateTag();

        CompoundNBT tagCompoundHeld = tagCompound.getCompound(TAG_HELD);
        held.write(tagCompoundHeld);
        tagCompound.put(TAG_HELD, tagCompoundHeld);

        tagCompound.putInt(TAG_RENDER_TYPE, renderType);

        stack.setTag(tagCompound);
    }

    public ItemStack getHeldStack(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains(TAG_HELD))
            return ItemStack.EMPTY;
        return ItemStack.read(tagCompound.getCompound(TAG_HELD));
    }

    public int getHeldRenderType(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains(TAG_RENDER_TYPE))
            return 0;
        return tagCompound.getInt(TAG_RENDER_TYPE);
    }

    /**
     * Try to insert held item into the tile
     *
     * @param context ItemUseContext that has the ItemBlockPlacedItems reference stack, which should contain renderType information.
     * @param tile    TilePlacedItems to insert into
     * @return true   if stack was at least partially successfully inserted
     */
    protected boolean tryInsertStack(ItemUseContext context, TilePlacedItems tile) {
        ItemStack heldItem = getHeldStack(context.getItem());
        int renderType = getHeldRenderType(context.getItem());
        ItemStack remainder = tile.insertStack(heldItem, renderType);
        tile.markDirty();
        tile.clean();
        if (remainder != heldItem) {
            setHeldStack(context.getItem(), remainder, renderType);
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (getHeldStack(context.getItem()).isEmpty()) return ActionResultType.FAIL;

        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();

        TilePlacedItems tile = null;
        if (world.getBlockState(pos).getBlock() == RegistryBlocks.placed_items) {
            tile = (TilePlacedItems) world.getTileEntity(pos);
        } else {
            BlockPos pos2 = pos.offset(facing);
            if (world.getBlockState(pos2).getBlock() == RegistryBlocks.placed_items) {
                tile = (TilePlacedItems) world.getTileEntity(pos2);
            }
        }

        PlayerEntity player = context.getPlayer();

        if (tile != null && tryInsertStack(context, tile)) {
            BlockState state = world.getBlockState(pos);
            SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            return ActionResultType.SUCCESS;
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.onItemUse(context);
    }

    @Override
    public boolean placeBlock(BlockItemUseContext context, BlockState newState) {
        if (getHeldStack(context.getItem()).isEmpty()) return false;
        if (!super.placeBlock(context, newState)) return false;

        TilePlacedItems tile = (TilePlacedItems) context.getWorld().getTileEntity(context.getPos());
        if (tile == null)
            return false;

        // Insert into freshly placed tile
        return tryInsertStack(context, tile);
    }
}
