package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.network.ClearCraftingGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ContainerCraftingBrowser extends ContainerBrowser
{
    private final static int HeightCrafter = 58;
    private final static int CraftingOffset = 59;

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public InventoryCraftResult craftResult = new InventoryCraftResult();

    private final World world;
    private final EntityPlayer player;

    public static int InventorySlotStart = FakeSlots;
    public static int CraftingSlotStart = FakeSlots + PlayerSlots + 1;

    Slot slotCraftResult;

    public ContainerCraftingBrowser(TileBrowser tileEntity, EntityPlayer player, boolean isClient)
    {
        super(tileEntity, player, isClient);

        this.world = tileEntity.getWorld();
        this.player = player;

        bindCraftingGrid(player.inventory, CraftingOffset);
    }

    protected void bindCraftingGrid(InventoryPlayer playerInventory, int top)
    {
        slotCraftResult = this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35 + top));

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
            this.slotChangedCraftingGrid(this.world, this.player, this.craftMatrix, this.craftResult);
        else
            super.onCraftMatrixChanged(inventoryIn);
    }

    @Override
    protected void slotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting inventoryCrafting, InventoryCraftResult craftingResult)
    {
        if (!world.isRemote)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)player;
            ItemStack itemstack = ItemStack.EMPTY;
            IRecipe irecipe = CraftingManager.findMatchingRecipe(inventoryCrafting, world);

            if (irecipe != null && (irecipe.isHidden() || !world.getGameRules().getBoolean("doLimitedCrafting") || entityplayermp.getRecipeBook().containsRecipe(irecipe)))
            {
                craftingResult.setRecipeUsed(irecipe);
                itemstack = irecipe.getCraftingResult(inventoryCrafting);
            }

            craftingResult.setInventorySlotContents(0, itemstack);
            entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, slotCraftResult.slotNumber, itemstack));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.world.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack.getCount() > 0)
                {
                    ItemHandlerHelper.giveItemToPlayer(playerIn, itemstack);
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
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int firstSlot = FakeSlots;
        int lastSlot = FakeSlots + PlayerSlots;

        if (!this.mergeItemStack(stack, firstSlot, lastSlot, false))
        {
            return ItemStack.EMPTY;
        }

        if (slotIndex == 0)
            slot.onSlotChange(stack, stackCopy);

        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.getCount() == stackCopy.getCount())
        {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);

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

        if (!this.world.isRemote)
        {
            IItemHandler parent = tile.getCombinedInventory();

            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (!isRemote && itemstack.getCount() > 0)
                {
                    ItemStack remaining = ItemStack.EMPTY;
                    if (parent != null)
                        remaining = AutomationHelper.insertItems(parent, itemstack);

                    if (remaining.getCount() > 0)
                    {
                        ItemHandlerHelper.giveItemToPlayer(playerIn, remaining);

                        if (remaining.getCount() != itemstack.getCount())
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

        this.onCraftMatrixChanged(craftMatrix);
    }
}
