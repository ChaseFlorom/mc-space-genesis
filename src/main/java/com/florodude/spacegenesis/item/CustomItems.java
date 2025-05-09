package com.florodude.spacegenesis.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.florodude.spacegenesis.SpaceGenesis;

public class CustomItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SpaceGenesis.MODID);

    public static final DeferredItem<Item> FLINT_SCRAPER = ITEMS.register("flint_scraper",
            () -> new FlintScraperItem());

    public static void registerToBus(net.neoforged.bus.api.IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
} 