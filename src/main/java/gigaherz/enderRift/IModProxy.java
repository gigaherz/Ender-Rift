package gigaherz.enderRift;

import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.network.UpdatePowerStatus;

public interface IModProxy
{
    default void preInit() {}

    default void init() {}

    default void handleSendSlotChanges(SendSlotChanges message) {}

    default void handleUpdateField(UpdateField message) {}

    default void handleUpdatePowerStatus(UpdatePowerStatus message) {}
}
