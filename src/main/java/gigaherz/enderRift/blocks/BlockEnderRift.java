package gigaherz.enderRift.blocks;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderRift
        extends Block
        implements ITileEntityProvider {

    public BlockEnderRift() {
        super(Material.rock);
        this.setTickRandomly(true);
        this.setCreativeTab(EnderRiftMod.tabEnderRift);
        this.setBlockTextureName("enderrift:block_rift");
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEnderRift();
    }
}
