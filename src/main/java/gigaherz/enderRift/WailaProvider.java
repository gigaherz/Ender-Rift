package gigaherz.enderRift;

import gigaherz.enderRift.blocks.TileEnderRift;
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

public class WailaProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

        if (config.getConfig("enderRift.block")) {
            TileEnderRift rift = (TileEnderRift) accessor.getTileEntity();

            int usedSlots = rift.countInventoryStacks();
            int exposedSlots = rift.getSizeInventory();
            int numPages = rift.getPageCount() / TileEnderRift.SlotsPerPage;

            currenttip.add(StatCollector.translateToLocalFormatted("text.blockEnderRift.waila.numPages", numPages));
            currenttip.add(StatCollector.translateToLocalFormatted("text.blockEnderRift.waila.usedSlots", usedSlots, exposedSlots));
            currenttip.add(StatCollector.translateToLocalFormatted("text.blockEnderRift.waila.energyStorage", rift.energyBuffer, rift.energyLimit));
            currenttip.add(StatCollector.translateToLocalFormatted("text.blockEnderRift.waila.energyUsageInsert", rift.getEnergyInsert()));
            currenttip.add(StatCollector.translateToLocalFormatted("text.blockEnderRift.waila.energyUsageExtract", rift.getEnergyExtract()));
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public static void callbackRegister(IWailaRegistrar registrar) {
        registrar.addConfig("Ender-Rift", "enderRift.block");
        registrar.registerBodyProvider(new WailaProvider(), TileEnderRift.class);
        registrar.registerNBTProvider(new WailaProvider(), TileEnderRift.class);
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {

        TileEnderRift rift = (TileEnderRift) te;

        rift.writeToNBT(tag);

        return tag;
    }
}
