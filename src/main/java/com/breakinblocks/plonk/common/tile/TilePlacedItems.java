package com.breakinblocks.plonk.common.tile;

import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TilePlacedItems extends TileEntity implements ISidedInventory {

    public static final String TAG_ITEMS = "Items";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_IS_BLOCK = "IsBlock";

    private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
    private boolean[] contentsIsBlock = new boolean[this.getSizeInventory()];
    private ItemStack[] contentsDisplay = new ItemStack[0];
    private boolean needsCleaning = true;

    public TilePlacedItems() {
    }

    /**
     * Clean the tile by compacting it and such, also updates contentsDisplay
     *
     * @return number of displayed stacks
     */
    private int clean() {
        int count = 0;
        for (int i = contents.length - 1; i >= 0; i--) {
            // TODO: Update null check
            if (contents[i] == null) continue;
            // Move stack towards start if has room
            if (i > 0 && contents[i - 1] == null) {
                contents[i - 1] = contents[i];
                // Also update the hitbox
                contentsIsBlock[i - 1] = contentsIsBlock[i];
                // TODO: Update null stack
                contents[i] = null;
            }
            // TODO: Update null check
            if (contents[i] != null) {
                count++;
            }
        }
        ItemStack[] displayedStacks = new ItemStack[count];
        System.arraycopy(contents, 0, displayedStacks, 0, count);
        contentsDisplay = displayedStacks;
        needsCleaning = false;
        return count;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList tagItems = tag.getTagList(TAG_ITEMS, 10);
        this.contents = new ItemStack[this.getSizeInventory()];
        this.contentsIsBlock = new boolean[this.getSizeInventory()];

        for (int i = 0; i < tagItems.tagCount(); i++) {
            NBTTagCompound tagItem = tagItems.getCompoundTagAt(i);
            int slot = tagItem.getByte(TAG_SLOT) & 255;
            boolean isBlock = tagItem.getBoolean(TAG_IS_BLOCK);

            if (slot >= 0 && slot < this.contents.length) {
                this.contents[slot] = ItemStack.loadItemStackFromNBT(tagItem);
                this.contentsIsBlock[slot] = isBlock;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList tagItems = new NBTTagList();

        for (int slot = 0; slot < this.contents.length; slot++) {
            if (this.contents[slot] != null) {
                NBTTagCompound tagItem = new NBTTagCompound();
                tagItem.setByte(TAG_SLOT, (byte) slot);
                tagItem.setBoolean(TAG_IS_BLOCK, this.contentsIsBlock[slot]);
                this.contents[slot].writeToNBT(tagItem);
                tagItems.appendTag(tagItem);
            }
        }

        tag.setTag(TAG_ITEMS, tagItems);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (needsCleaning) {
            if (clean() <= 0) {
                this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
            } else {
                int meta = this.getBlockMetadata();
                if (meta > 5) {
                    meta = 0;
                }
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, meta, 2);
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
        }
    }

    @Override
    public void markDirty() {
        needsCleaning = true;
        super.markDirty();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
        needsCleaning = true;
    }

    @Override
    public int getSizeInventory() {
        return 4;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return contents[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int maxAmount) {
        //TODO: Update null items
        ItemStack current = contents[slot];
        if (maxAmount > 0 && current != null && current.stackSize > 0) {
            int amount = Math.min(current.stackSize, maxAmount);

            ItemStack extractedStack = current.copy();
            extractedStack.stackSize = amount;

            current.stackSize -= amount;

            if (current.stackSize <= 0) {
                contents[slot] = null;
            }

            this.markDirty();
            return extractedStack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return contents[slot];
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        //TODO: Update null items
        if (stack == null || stack.stackSize <= 0) {
            contents[slot] = null;
        } else {
            if (stack.stackSize > this.getInventoryStackLimit()) {
                stack.stackSize = this.getInventoryStackLimit();
            }
            contents[slot] = stack;
        }
        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.placed_items";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return true;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    public ItemStack[] getContentsDisplay() {
        return contentsDisplay;
    }

    public boolean[] getContentsIsBlock() {
        return contentsIsBlock;
    }

    /**
     * Attempt to insert the given stack, also recording the isBlock status if needed
     *
     * @param stack   to be inserted
     * @param isBlock if the stack to be inserted should be treated as a block for display
     * @return the remaining items if successful, or the original stack if not
     */
    public ItemStack insertStack(ItemStack stack, boolean isBlock) {
        ItemUtils.InsertStackResult result = ItemUtils.insertStackAdv(this, stack);
        if (result.remainder != stack) {
            for (int slot : result.slots) {
                contentsIsBlock[slot] = isBlock;
            }
        }
        return result.remainder;
    }
}
