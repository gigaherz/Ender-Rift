package gigaherz.enderRift;

import gigaherz.enderRift.network.SetSpecialSlot;
import gigaherz.enderRift.network.UpdateField;

public interface IModProxy
{
    void preInit();

    void init();

    void handleSetSpecialSlot(SetSpecialSlot message);

    void handleUpdateField(UpdateField message);
}
