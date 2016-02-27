package gigaherz.capabilities.api.energy;

public interface IEnergyPersist extends IEnergyHandler
{
    /**
     * Allows an IEnergyHandler to be saved and loaded using the default IStorage.
     * This is NOT meant to be called externally, as it bypasses insert/extract restrictions!
     *
     * @param energy The stored energy value being loaded from NBT.
     */
    void setEnergy(int energy);
}
