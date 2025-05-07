package com.florodude.spacegenesis.dimension;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.florodude.spacegenesis.SpaceGenesis;
import java.util.OptionalLong;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.ConstantInt;

public class AsteroidDimension {
    public static final ResourceKey<Level> ASTEROID_LEVEL = ResourceKey.create(Registries.DIMENSION, 
        ResourceLocation.parse(SpaceGenesis.MODID + ":asteroid"));
    
    public static final ResourceKey<DimensionType> ASTEROID_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        ResourceLocation.parse(SpaceGenesis.MODID + ":asteroid"));
    
    public static final ResourceKey<LevelStem> ASTEROID_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM,
        ResourceLocation.parse(SpaceGenesis.MODID + ":asteroid"));

    public static final DeferredRegister<DimensionType> DIMENSION_TYPES = 
        DeferredRegister.create(Registries.DIMENSION_TYPE, SpaceGenesis.MODID);

    public static void init() {
        DIMENSION_TYPES.register("asteroid", () -> {
            DimensionType.MonsterSettings monsterSettings = new DimensionType.MonsterSettings(
                false, // piglinSafe
                false, // hasRaids
                ConstantInt.of(0), // monsterSpawnLightTest
                0 // monsterSpawnBlockLightLimit
            );
            return new DimensionType(
                OptionalLong.empty(), // fixed time
                true, // has skylight
                false, // has ceiling
                false, // ultrawarm
                true, // natural
                1.0, // coordinate scale
                true, // bed works
                false, // respawn anchor works
                0, // min y
                256, // height
                256, // logical height
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                ResourceLocation.parse(SpaceGenesis.MODID + ":asteroid"), // effects location
                0.0f, // ambient light
                monsterSettings // monster settings
            );
        });
    }
} 