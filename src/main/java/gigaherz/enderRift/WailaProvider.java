package gigaherz.enderRift;

import gigaherz.enderRift.blocks.TileEnderRift;
import gigaherz.enderRift.blocks.TileEnderRiftCorner;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class WailaProvider implements IWailaDataProvider
{

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {

        if (config.getConfig("enderRift.block"))
        {
            NBTTagCompound tag = accessor.getNBTData();

            currenttip.add(StatCollector.translateToLocalFormatted("text." + EnderRiftMod.MODID + ".blockEnderRift.waila.isFormed", tag.getBoolean("isFormed")));
            if(tag.getBoolean("isFormed"))
            {
                currenttip.add(StatCollector.translateToLocalFormatted("text." + EnderRiftMod.MODID + ".blockEnderRift.waila.riftId", tag.getInteger("riftId")));
            }
            currenttip.add(StatCollector.translateToLocalFormatted("text." + EnderRiftMod.MODID + ".blockEnderRift.waila.usedSlots", tag.getInteger("usedSlots"), tag.getInteger("exposedSlots")));
            currenttip.add(StatCollector.translateToLocalFormatted("text." + EnderRiftMod.MODID + ".blockEnderRift.waila.energyUsageInsert", tag.getInteger("energyInsert")));
            currenttip.add(StatCollector.translateToLocalFormatted("text." + EnderRiftMod.MODID + ".blockEnderRift.waila.energyUsageExtract", tag.getInteger("energyExtract")));
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    public static void callbackRegister(IWailaRegistrar registrar)
    {
        registrar.addConfig("Ender-Rift", "enderRift.block");

        WailaProvider instance = new WailaProvider();
        registrar.registerBodyProvider(instance, TileEnderRift.class);
        registrar.registerNBTProvider(instance, TileEnderRift.class);
        registrar.registerBodyProvider(instance, TileEnderRiftCorner.class);
        registrar.registerNBTProvider(instance, TileEnderRiftCorner.class);
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z)
    {
        TileEnderRift rift;

        if(te instanceof TileEnderRiftCorner)
            rift = (TileEnderRift)((TileEnderRiftCorner)te).getParent();
        else
            rift = (TileEnderRift) te;

        tag.setInteger("usedSlots", rift.countInventoryStacks());
        tag.setInteger("exposedSlots", rift.getSizeInventory());
        tag.setInteger("energyInsert", rift.getEnergyInsert());
        tag.setInteger("energyExtract", rift.getEnergyExtract());
        tag.setBoolean("isFormed", rift.getBlockMetadata() != 0);
        tag.setInteger("riftId", rift.riftId);

        return tag;
    }
}
