package gigaherz.enderRift;

import gigaherz.enderRift.network.SetSpecialSlot;

public interface IModProxy
{
    void preInit();

    void init();

    void handleSetSpecialSlot(SetSpecialSlot message);
}
