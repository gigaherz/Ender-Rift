package gigaherz.capabilities.api.energy;

public interface IEnergyHandler
{
    /**
     * Obtains the maximum contained energy.
     *
     * @return the current capacity.
     */
    int getCapacity();


    /**
     * Obtains the current contained energy.
     *
     * @return the current energy level.
     */
    int getEnergy();

    /**
     * Attempts to extract the specified amount of energy.
     *
     * @param maxExtract The maximum amount of energy to extract.
     * @param simulate   If true, the energy is not subtracted from the buffer.
     * @return The energy actually extracted.
     */
    int extractEnergy(int maxExtract, boolean simulate);


    /**
     * Attempts to insert the specified amount of energy.
     *
     * @param maxReceive The maximum amount of energy to insert.
     * @param simulate   If true, the energy is not added to the buffer.
     * @return The energy actually inserted.
     */
    int insertEnergy(int maxReceive, boolean simulate);
}
