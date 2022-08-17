package dev.gigaherz.enderrift.debug;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class DebugKeyCache
{

	private DebugKeyCache()
	{
		throw new UnsupportedOperationException();
	}

	private static ResourceLocation[] items;

	public static int size()
	{
		return items == null ? 0 : items.length;
	}

	public static Holder<Item> getItem(int index)
	{
		ResourceLocation location = get(index);
		if (location == null)
		{
			return null;
		}
		return ForgeRegistries.ITEMS.getHolder(location).orElse(null);
	}

	public static ResourceLocation get(int index)
	{
		if (index >= size() || index < 0)
		{
			return null;
		}
		return items[index];
	}

	public static void update()
	{
		items = ForgeRegistries.ITEMS.getKeys().toArray(ResourceLocation[]::new);
	}

}
