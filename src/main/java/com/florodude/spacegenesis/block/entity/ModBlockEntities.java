package com.florodude.spacegenesis.block.entity;

import com.florodude.spacegenesis.block.CustomBlocks;
import com.florodude.spacegenesis.SpaceGenesis;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SpaceGenesis.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagnetBlockEntity>> MAGNET_BE =
        BLOCK_ENTITIES.register("magnet",
            () -> BlockEntityType.Builder.of(MagnetBlockEntity::new, CustomBlocks.MAGNET_BLOCK.get()).build(null));
} 