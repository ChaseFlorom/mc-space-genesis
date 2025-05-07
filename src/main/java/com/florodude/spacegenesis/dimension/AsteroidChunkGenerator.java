package com.florodude.spacegenesis.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.GenerationStep;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class AsteroidChunkGenerator extends ChunkGenerator {
    public static final MapCodec<AsteroidChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
        ).apply(instance, AsteroidChunkGenerator::new));

    public AsteroidChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carvingStep) {
        // No carvers in asteroid dimension
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        RandomSource randomSource = RandomSource.create(region.getSeed());
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Generate height using simplex noise
                double noise = generateNoise(chunkX * 16 + x, chunkZ * 16 + z, randomSource);
                int height = (int) (noise * 20) + 64; // Base height of 64, with ±20 block variation

                for (int y = region.getMinBuildHeight(); y < region.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (y < height) {
                        chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                    } else {
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    private double generateNoise(int x, int z, RandomSource random) {
        // Simple simplex-like noise function
        double scale = 0.01;
        double nx = x * scale;
        double nz = z * scale;
        
        // Multiple octaves of noise for more natural terrain
        double value = 0;
        double amplitude = 1.0;
        double frequency = 1.0;
        
        for (int i = 0; i < 4; i++) {
            value += amplitude * (Mth.sin((float)(nx * frequency)) * Mth.cos((float)(nz * frequency)));
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        
        return (value + 1.0) * 0.5; // Normalize to 0-1
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // No mob spawning in asteroid dimension
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor level, RandomState random) {
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(level.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        // Optionally add debug info here
    }
} 