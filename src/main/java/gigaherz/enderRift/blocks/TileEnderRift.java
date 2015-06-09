package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class TileEnderRift
        extends TileEntity
        implements IEnergyReceiver, IInventory {

    public static final float PowerPerInsertionConstant = 1;
    public static final float PowerPerInsertionLinear = 1;


    public static final float PowerPerExtractionConstant = 1;
    public static final float PowerPerExtractionLinear = 0;

    public static final int SlotsPerPage = 27;

    private List<ItemStack[]> inventoryPages;

    public final int energyLimit = 100000;
    public int energyBuffer = 0;

    public TileEnderRift() {
        inventoryPages = new ArrayList<ItemStack[]>();
    }

    public int getEnergyInsert() {
        return (int) Math.ceil(PowerPerInsertionConstant + (inventoryPages.size() * PowerPerInsertionLinear));
    }

    public int getEnergyExtract() {
        return (int) Math.ceil(PowerPerExtractionConstant + (inventoryPages.size() * PowerPerExtractionLinear));
    }

    public int getPageCount() {
        return inventoryPages.size();
    }

    public int countInventoryStacks() {
        int count = 0;
        for (ItemStack[] page : inventoryPages) {
            for (ItemStack stack : page) {
                if (stack != null)
                    count++;
            }
        }
        return count;
    }

    @Override
    public int getSizeInventory() {
        return (inventoryPages.size() + 1) * SlotsPerPage;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {

        int page = slotIndex / SlotsPerPage;
        int slot = slotIndex % SlotsPerPage;

        if (page >= inventoryPages.size())
            return null;

        ItemStack[] inventory = inventoryPages.get(page);

        return inventory[slot];
    }

    private void setStackInSlotInternal(int slotIndex, ItemStack stack) {

        int page = slotIndex / SlotsPerPage;
        int slot = slotIndex % SlotsPerPage;

        while (page >= inventoryPages.size()) {
            inventoryPages.add(new ItemStack[SlotsPerPage]);
        }

        ItemStack[] inventory = inventoryPages.get(page);

        inventory[slot] = stack;

        this.markDirty();
    }

    private void removeStackFromSlotInternal(int slotIndex) {

        int page = slotIndex / SlotsPerPage;
        int slot = slotIndex % SlotsPerPage;

        if (page >= inventoryPages.size())
            return;

        ItemStack[] inventory = inventoryPages.get(page);

        inventory[slot] = null;

        if (isPageEmpty(inventory))
            inventoryPages.remove(page);

        this.markDirty();
    }

    private boolean isPageEmpty(ItemStack[] inventory) {
        for (ItemStack stack : inventory) {
            if (stack != null)
                return false;
        }
        return true;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {

        if (stack == null) {
            ItemStack oldStack = getStackInSlot(slot);

            if (oldStack == null)
                return;

            int powerCost = getEnergyExtract();

            if (energyBuffer >= powerCost) {
                removeStackFromSlotInternal(slot);
                energyBuffer -= powerCost;
            }

            return;
        }

        int powerCost = getEnergyInsert();

        if (energyBuffer >= powerCost) {
            setStackInSlotInternal(slot, stack);
            energyBuffer -= powerCost;
        } else {
            stack.stackSize = 0;
        }

        if (stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack == null)
            return null;

        if (stack.stackSize <= amount) {
            setInventorySlotContents(slotIndex, null);
        } else {
            stack = stack.splitStack(amount);

            if (stack.stackSize == 0) {
                setInventorySlotContents(slotIndex, null);
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
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

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        NBTTagList nbtTagList = nbtTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        inventoryPages.clear();

        for (int i = 0; i < nbtTagList.tagCount(); ++i) {
            NBTTagCompound nbtTagCompound1 = nbtTagList.getCompoundTagAt(i);
            int p = nbtTagCompound1.getInteger("Page") & 255;
            int j = nbtTagCompound1.getByte("Slot") & 255;

            setStackInSlotInternal(p * SlotsPerPage + j, ItemStack.loadItemStackFromNBT(nbtTagCompound1));
        }

        energyBuffer = nbtTagCompound.getInteger("Energy");
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        NBTTagList nbtTagList = new NBTTagList();

        for (int j = 0; j < inventoryPages.size(); j++) {
            ItemStack[] page = inventoryPages.get(j);
            for (int i = 0; i < page.length; ++i) {
                ItemStack stack = page[i];
                if (stack != null) {
                    NBTTagCompound nbtTagCompound1 = new NBTTagCompound();
                    nbtTagCompound1.setInteger("Page", j);
                    nbtTagCompound1.setByte("Slot", (byte) i);
                    stack.writeToNBT(nbtTagCompound1);
                    nbtTagList.appendTag(nbtTagCompound1);
                }
            }
        }

        nbtTagCompound.setTag("Items", nbtTagList);
        nbtTagCompound.setInteger("Energy", energyBuffer);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        readFromNBT(packet.func_148857_g());
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        int receive = Math.min(maxReceive, energyLimit - energyBuffer);
        if (!simulate)
            energyBuffer += receive;
        return receive;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energyBuffer;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return energyLimit;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

}