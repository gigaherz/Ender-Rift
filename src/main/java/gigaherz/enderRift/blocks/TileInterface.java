package gigaherz.enderRift.blocks;

import gigaherz.api.automation.IInventoryAutomation;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.common.util.Constants;

public class TileInterface extends TileEntity
    implements IInventory, ITickable
{
    static final int FilterCount = 9;

    final ItemStack[] filters = new ItemStack[FilterCount];
    final ItemStack[] outputs = new ItemStack[FilterCount];

    boolean parentSearched;
    IInventoryAutomation parent;

    public IInventoryAutomation getParent()
    {
        if(!parentSearched)
        {
            IBlockState state = worldObj.getBlockState(getPos());
            TileEntity te = worldObj.getTileEntity(pos.offset(state.getValue(BlockInterface.FACING)));
            if(te instanceof IInventoryAutomation)
            {
                parent = (IInventoryAutomation)te;
            }
            parentSearched = true;
        }
        return parent;
    }

    @Override
    public void markDirty()
    {
        parentSearched = false;
        parent = null;
        super.markDirty();
    }

    @Override
    public void update()
    {
        if(getParent() == null)
            return;

        for(int i=0;i<FilterCount;i++)
        {
            if(filters[i] != null)
            {
                if (outputs[i] == null)
                {
                    int free = 64;
                    outputs[i] = getParent().extractItems(filters[i], free);
                }
                else if (outputs[i].isItemEqual(filters[i]))
                {
                    int free = outputs[i].getMaxStackSize() - outputs[i].stackSize;
                    if (free > 0)
                    {
                        ItemStack extracted = getParent().extractItems(filters[i], free);
                        if(extracted != null)
                            outputs[i].stackSize += extracted.stackSize;
                    }
                }
                else
                {
                    outputs[i] = getParent().pushItems(outputs[i]);
                }
            }
            else if (outputs[i] != null)
            {
               outputs[i] = getParent().pushItems(outputs[i]);
            }
        }
    }

    @Override
    public int getSizeInventory()
    {
        return FilterCount;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if(index < 0 || index >= outputs.length)
            return null;
        return outputs[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if(index < 0 || index >= outputs.length)
            return null;

        if (outputs[index] == null)
            return null;

        if(count > outputs[index].stackSize)
            count = outputs[index].stackSize;

        ItemStack result = outputs[index].splitStack(count);

        if (outputs[index].stackSize <= 0)
        {
            outputs[index] = null;
        }

        this.markDirty();

        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if(index < 0 || index >= outputs.length)
            return null;

        if (outputs[index] == null)
            return null;

        ItemStack itemstack = outputs[index];
        outputs[index] = null;
        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if(index < 0 || index >= outputs.length)
            return;

        outputs[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getName()
    {
        return "container." + EnderRiftMod.MODID + ".interface";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList _filters = compound.getTagList("Filters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _filters.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = _filters.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < filters.length)
            {
                filters[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
        
        NBTTagList _outputs = compound.getTagList("Outputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _outputs.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = _outputs.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < outputs.length)
            {
                outputs[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagList _filters = new NBTTagList();
        for (int i = 0; i < filters.length; ++i)
        {
            if (filters[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                filters[i].writeToNBT(nbttagcompound);
                _filters.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Filters", _filters);
        
        NBTTagList _outputs = new NBTTagList();
        for (int i = 0; i < outputs.length; ++i)
        {
            if (outputs[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                outputs[i].writeToNBT(nbttagcompound);
                _outputs.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Outputs", _outputs);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    public void clear()
    {
        for (int i = 0; i < filters.length; ++i)
        {
            filters[i] = null;
        }
        for (int i = 0; i < outputs.length; ++i)
        {
            outputs[i] = null;
        }
    }

    public IInventory inventoryFilter()
    {
        return new IInventory()
        {

            @Override
            public void markDirty()
            {
            }

            @Override
            public int getSizeInventory()
            {
                return FilterCount;
            }

            @Override
            public ItemStack getStackInSlot(int index)
            {
                if(index < 0 || index >= filters.length)
                    return null;
                return filters[index];
            }

            @Override
            public ItemStack decrStackSize(int index, int count)
            {
                if(index < 0 || index >= filters.length)
                    return null;

                if (filters[index] == null)
                    return null;

                if(count > filters[index].stackSize)
                    count = filters[index].stackSize;

                ItemStack result = filters[index].splitStack(count);

                if (filters[index].stackSize <= 0)
                {
                    filters[index] = null;
                }

                this.markDirty();

                return result;
            }

            @Override
            public ItemStack removeStackFromSlot(int index)
            {
                if(index < 0 || index >= filters.length)
                    return null;

                if (filters[index] == null)
                    return null;

                ItemStack itemstack = filters[index];
                filters[index] = null;
                return itemstack;
            }

            @Override
            public void setInventorySlotContents(int index, ItemStack stack)
            {
                filters[index] = stack;

                if (stack != null && stack.stackSize > this.getInventoryStackLimit())
                {
                    stack.stackSize = this.getInventoryStackLimit();
                }

                this.markDirty();
            }

            @Override
            public String getName()
            {
                return null;
            }

            @Override
            public boolean hasCustomName()
            {
                return false;
            }

            @Override
            public IChatComponent getDisplayName()
            {
                return null;
            }

            @Override
            public int getInventoryStackLimit()
            {
                return 1;
            }

            @Override
            public boolean isUseableByPlayer(EntityPlayer player)
            {
                return true;
            }

            @Override
            public void openInventory(EntityPlayer player)
            {
            }

            @Override
            public void closeInventory(EntityPlayer player)
            {
            }

            @Override
            public boolean isItemValidForSlot(int index, ItemStack stack)
            {
                return true;
            }

            @Override
            public int getField(int id)
            {
                return 0;
            }

            @Override
            public void setField(int id, int value)
            {
            }

            @Override
            public int getFieldCount()
            {
                return 0;
            }

            @Override
            public void clear()
            {
                for (int i = 0; i < filters.length; ++i)
                {
                    filters[i] = null;
                }
            }
        };
    }
}
