package net.hollowed.antique.index;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Instrument;

public interface AntiqueInstruments {
	ResourceKey<Instrument> WARHORN = of("warhorn");

	@SuppressWarnings("all")
	private static ResourceKey<Instrument> of(String id) {
		return ResourceKey.create(Registries.INSTRUMENT, Identifier.minecraft(id));
	}
}
