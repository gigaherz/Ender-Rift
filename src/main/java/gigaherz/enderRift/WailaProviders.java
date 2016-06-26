package gigaherz.enderRift;

import gigaherz.enderRift.blocks.BlockStructure;
import gigaherz.enderRift.blocks.TileEnderRift;
import gigaherz.enderRift.blocks.TileEnderRiftCorner;
import gigaherz.enderRift.blocks.TileGenerator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.List;

public class WailaProviders
{
    public static void callbackRegister(IWailaRegistrar registrar)
    {
        registrar.addConfig("Ender-Rift", "enderRift.block");

        {
            RiftTooltipProvider instance = new RiftTooltipProvider();
            registrar.registerBodyProvider(instance, TileEnderRift.class);
            registrar.registerNBTProvider(instance, TileEnderRift.class);
            registrar.registerStackProvider(instance, TileEnderRiftCorner.class);
            registrar.registerBodyProvider(instance, TileEnderRiftCorner.class);
            registrar.registerNBTProvider(instance, TileEnderRiftCorner.class);
        }

        {
            StructureTooltipProvider instance = new StructureTooltipProvider();
            registrar.registerStackProvider(instance, BlockStructure.class);
        }

        {
            GeneratorTooltipProvider instance = new GeneratorTooltipProvider();
            registrar.registerBodyProvider(instance, TileGenerator.class);
            registrar.registerNBTProvider(instance, TileGenerator.class);
        }
    }


    @Optional.Interface(modid = "Waila", iface = "mcp.mobius.waila.api.IWailaDataProvider")
    public static class GeneratorTooltipProvider implements IWailaDataProvider
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
            if (config.getConfig("enderRift.blockGenerator"))
            {
                NBTTagCompound tag = accessor.getNBTData();

                if (tag.getInteger("powerGen") > 0)
                {
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".generator.status.generating", tag.getInteger("powerGen")));
                }
                else if (tag.getBoolean("isBurning"))
                {
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".generator.status.heating"));
                }
                else
                {
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".generator.status.idle"));
                }

                currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".generator.heat", tag.getInteger("heat")));
                currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".generator.energy", tag.getInteger("energy"), TileGenerator.PowerLimit));
            }

            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
        {
            TileGenerator rift = (TileGenerator) te;

            tag.setBoolean("isBurning", rift.isBurning());
            tag.setInteger("powerGen", rift.getGenerationPower());
            tag.setInteger("energy", rift.getContainedEnergy());
            tag.setInteger("heat", rift.getHeatValue());

            return tag;
        }
    }

    @Optional.Interface(modid = "Waila", iface = "mcp.mobius.waila.api.IWailaDataProvider")
    public static class StructureTooltipProvider implements IWailaDataProvider
    {
        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return new ItemStack(EnderRiftMod.rift);
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return currenttip;
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
        {
            return tag;
        }
    }

    @Optional.Interface(modid = "Waila", iface = "mcp.mobius.waila.api.IWailaDataProvider")
    public static class RiftTooltipProvider implements IWailaDataProvider
    {
        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return new ItemStack(EnderRiftMod.structure);
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return currenttip;
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            if (config.getConfig("enderRift.blockEnderRift"))
            {
                NBTTagCompound tag = accessor.getNBTData();

                if (tag != null && tag.hasKey("isFormed"))
                {
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.isFormed", tag.getBoolean("isFormed")));
                    if (tag.getBoolean("isFormed"))
                    {
                        currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.riftId", tag.getInteger("riftId")));
                        currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.rf", tag.getInteger("energy"), tag.getInteger("energyTotal")));
                    }
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.usedSlots", tag.getInteger("usedSlots")));
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.energyUsageInsert", tag.getInteger("energyInsert")));
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.energyUsageExtract", tag.getInteger("energyExtract")));
                }
                else
                {
                    currenttip.add(I18n.format("text." + EnderRiftMod.MODID + ".rift.waila.isFormed", false));
                }
            }

            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
        {
            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
        {
            TileEnderRift rift;

            if (te instanceof TileEnderRiftCorner)
                rift = (TileEnderRift) ((TileEnderRiftCorner) te).getParent();
            else
                rift = (TileEnderRift) te;

            tag.setInteger("usedSlots", rift.countInventoryStacks());
            tag.setInteger("energyInsert", (int) Math.ceil(rift.getEnergyInsert()));
            tag.setInteger("energyExtract", (int) Math.ceil(rift.getEnergyExtract()));
            tag.setBoolean("isFormed", rift.getBlockMetadata() != 0);
            tag.setInteger("riftId", rift.getRiftId());
            tag.setInteger("energy", rift.getEnergyBuffer().getEnergy());
            tag.setInteger("energyTotal", rift.getEnergyBuffer().getCapacity());

            return tag;
        }
    }
}
