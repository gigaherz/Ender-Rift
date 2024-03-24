package dev.gigaherz.enderrift.generator;

import net.minecraft.world.inventory.ContainerData;

public class ClientFields implements ContainerData
{
    public int heatLevel;
    public int burnTimeRemaining;
    public int currentItemBurnTime;
    public int energy;

    @Override
    public int get(int field)
    {
        return switch (field)
        {
            case 0 -> burnTimeRemaining;
            case 1 -> currentItemBurnTime;
            case 2 -> energy & 0xFFFF;
            case 3 -> energy >> 16;
            case 4 -> heatLevel;
            default -> 0;
        };
    }

    @Override
    public void set(int field, int value)
    {
        switch (field)
        {
            case 0 -> burnTimeRemaining = value;
            case 1 -> currentItemBurnTime = value;
            case 2 -> energy = (energy & 0xFFFF0000) | (value & 0xFFFF);
            case 3 -> energy = (energy & 0xFFFF) | (value << 16);
            case 4 -> heatLevel = value;
        }
    }

    @Override
    public int getCount()
    {
        return 5;
    }
}
