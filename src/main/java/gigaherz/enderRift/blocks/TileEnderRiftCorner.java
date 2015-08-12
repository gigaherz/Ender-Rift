package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.network.NetworkRegistry;
import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.network.ValueUpdate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class TileEnderRiftCorner
        extends TileEntity
        implements IEnergyReceiver
{
    int xParent = 0;
    int yParent;
    int zParent;
    TileEnderRift energyParent;

    public IEnergyReceiver getParent()
    {
        if(energyParent == null)
        {
            int meta = getBlockMetadata();
            xParent = xCoord  + ((meta&1)!=0?-1:1);
            yParent = yCoord  + ((meta&2)!=0?-1:1);
            zParent = zCoord  + ((meta&4)!=0?-1:1);
            TileEntity te = worldObj.getTileEntity(xParent, yParent, zParent);
            if(te instanceof TileEnderRift)
            {
                energyParent = (TileEnderRift) te;
            }
        }
        return energyParent;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
    {
        IEnergyReceiver parent = getParent();
        if(parent == null)
            return 0;
        return parent.receiveEnergy(from, maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from)
    {
        IEnergyReceiver parent = getParent();
        if(parent == null)
            return 0;
        return getParent().getEnergyStored(from);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        IEnergyReceiver parent = getParent();
        if(parent == null)
            return 0;
        return getParent().getMaxEnergyStored(from);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from)
    {
        return true;
    }

}