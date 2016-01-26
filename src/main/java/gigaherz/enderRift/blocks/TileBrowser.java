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
    IInventoryAutomation automation;

    public int changeCount = 1;

    public IBrowsableInventory getParent()
    {
        IInventoryAutomation automation = getAutomation();
        if (automation instanceof IBrowsableInventory)
        {
            return (IBrowsableInventory) automation;
        }
        return null;
    }

    public IInventoryAutomation getAutomation()
    {
        if (!parentSearched)
        {
            IBlockState state = worldObj.getBlockState(getPos());
            EnumFacing facing = state.getValue(BlockBrowser.FACING);
            TileEntity te = worldObj.getTileEntity(pos.offset(facing));
            if (te != null)
            {
                automation = AutomationHelper.get(te, facing.getOpposite());
            }
            parentSearched = true;
        }
        return automation;
    }

    @Override
    public void markDirty()
    {
        changeCount++;
        parentSearched = false;
        automation = null;
        super.markDirty();
    }
}
