package com.florodude.spacegenesis.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import com.florodude.spacegenesis.SpaceGenesis;
import com.florodude.spacegenesis.block.MineralDepositBlock;

public class CustomBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SpaceGenesis.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SpaceGenesis.MODID);

    public static final DeferredBlock<Block> MINERAL_DEPOSIT_BLOCK = BLOCKS.register("mineral_deposit",
            () -> new MineralDepositBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .noOcclusion()
            ));
    public static final DeferredItem<BlockItem> MINERAL_DEPOSIT_ITEM = ITEMS.registerSimpleBlockItem("mineral_deposit", MINERAL_DEPOSIT_BLOCK);

    public static void registerToBus(net.neoforged.bus.api.IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
} 