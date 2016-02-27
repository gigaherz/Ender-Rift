package gigaherz.capabilities.api.tweakable;

public interface ITweakable
{
    /**
     * Requests a configuration UI or mode change.
     *
     * The hitX/Y/Z params can be used to detect which area was activated.
     */
    void configure(float hitX, float hitY, float hitZ);

    /**
     * Requests the machine to dismantle itself and become and item.
     */
    void dismantle();
}
