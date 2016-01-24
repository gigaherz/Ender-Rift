package gigaherz.enderRift.blocks;

import gigaherz.api.automation.AutomationHelper;
import gigaherz.api.automation.IBrowsableInventory;
import gigaherz.api.automation.IInventoryAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileBrowser extends TileEntity
{
    boolean parentSearched;
    IBrowsableInventory parent;

    public int changeCount = 1;

    public IBrowsableInventory getParent()
    {
        if (!parentSearched)
        {
            IBlockState state = worldObj.getBlockState(getPos());
            EnumFacing facing = state.getValue(BlockBrowser.FACING);
            TileEntity te = worldObj.getTileEntity(pos.offset(facing));
            if (te != null)
            {
                IInventoryAutomation inv = AutomationHelper.get(te, facing.getOpposite());
                if(inv instanceof IBrowsableInventory)
                {
                    parent = (IBrowsableInventory)inv;
                }
            }
            parentSearched = true;
        }
        return parent;
    }

    @Override
    public void markDirty()
    {
        changeCount++;
        parentSearched = false;
        parent = null;
        super.markDirty();
    }
}
