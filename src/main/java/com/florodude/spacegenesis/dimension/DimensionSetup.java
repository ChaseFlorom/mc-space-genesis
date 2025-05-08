package com.florodude.spacegenesis.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.florodude.spacegenesis.SpaceGenesis;
import com.florodude.spacegenesis.dimension.SpaceDimension;

@EventBusSubscriber(modid = SpaceGenesis.MODID)
public class DimensionSetup {
    public static void init(IEventBus modEventBus) {
        AsteroidDimension.DIMENSION_TYPES.register(modEventBus);
        SpaceDimension.DIMENSION_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel().dimensionType().equals(AsteroidDimension.ASTEROID_DIMENSION_TYPE)) {
            // Set spawn point in the asteroid dimension
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.setDefaultSpawnPos(new BlockPos(0, 100, 0), 0.0F);
                // Clear a 5x5x5 area around spawn to air
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            BlockPos clearPos = new BlockPos(dx, 100 + dy, dz);
                            serverLevel.setBlockAndUpdate(clearPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
        if (event.getLevel().dimensionType().equals(SpaceDimension.SPACE_DIMENSION_TYPE)) {
            // Set spawn point in the space dimension
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.setDefaultSpawnPos(new BlockPos(0, 100, 0), 0.0F);
                // Clear a 5x5x5 area around spawn to air
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            BlockPos clearPos = new BlockPos(dx, 100 + dy, dz);
                            serverLevel.setBlockAndUpdate(clearPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
} 