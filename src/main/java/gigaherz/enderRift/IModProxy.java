package gigaherz.enderRift;

import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;

public interface IModProxy
{
    void preInit();

    void init();

    void handleSendSlotChanges(SendSlotChanges message);

    void handleUpdateField(UpdateField message);
}
