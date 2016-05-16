package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.IInventoryAutomation;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.network.ClearCraftingGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

public class ContainerCraftingBrowser extends ContainerBrowser
{
    private final static int HeightCrafter = 58;
    private final static int CraftingOffset = 59;

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public IInventory craftResult = new InventoryCraftResult();
    private World worldObj;

    public ContainerCraftingBrowser(TileBrowser tileEntity, EntityPlayer player, boolean isClient)
    {
        super(tileEntity, player, isClient);

        this.worldObj = tileEntity.getWorld();

        bindCraftingGrid(player.inventory, CraftingOffset);
    }

    protected void bindCraftingGrid(InventoryPlayer playerInventory, int top)
    {
        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35 + top));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18 + top));
            }
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    @Override
    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        bindPlayerInventory(playerInventory, Top + FakeRows * SlotHeight + 14 + HeightCrafter);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        if (inventoryIn == craftMatrix)
            this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
        else
            super.onCraftMatrixChanged(inventoryIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.worldObj.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack != null)
                {
                    playerIn.dropItem(itemstack, false);
                }
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        if (slotIndex < FakeSlots + PlayerSlots)
        {
            return super.transferStackInSlot(player, slotIndex);
        }

        Slot slot = this.inventorySlots.get(slotIndex);

        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int firstSlot = FakeSlots;
        int lastSlot = FakeSlots + PlayerSlots;

        if (!this.mergeItemStack(stack, firstSlot, lastSlot, false))
        {
            return null;
        }

        if (slotIndex == 0)
            slot.onSlotChange(stack, stackCopy);

        if (stack.stackSize == 0)
        {
            slot.putStack(null);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.stackSize == stackCopy.stackSize)
        {
            return null;
        }

        slot.onPickupFromSlot(player, stack);

        return stackCopy;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }

    public void clearCraftingGrid(EntityPlayer playerIn)
    {
        boolean isRemote = tile.getWorld().isRemote;

        if (!this.worldObj.isRemote)
        {
            IInventoryAutomation parent = tile.getAutomation();

            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (!isRemote && itemstack != null)
                {
                    ItemStack remaining = null;
                    if (parent != null)
                        remaining = parent.insertItems(itemstack);

                    if (remaining != null)
                    {
                        playerIn.dropItem(remaining, false);

                        if (remaining.stackSize != itemstack.stackSize)
                            tile.markDirty();
                    }
                    else
                    {
                        tile.markDirty();
                    }

                }
            }
        }

        if (isRemote)
        {
            EnderRiftMod.channel.sendToServer(new ClearCraftingGrid(windowId));
        }
    }
}
