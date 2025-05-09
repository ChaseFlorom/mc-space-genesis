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
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import java.util.ArrayList;
import com.florodude.spacegenesis.block.CustomBlocks;

public class AsteroidChunkGenerator extends ChunkGenerator {
    public static final MapCodec<AsteroidChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
        ).apply(instance, AsteroidChunkGenerator::new));

    // Use a static seed for seamless base terrain noise
    private static final long BASE_TERRAIN_SEED = 12345L;

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

        // Precompute craters for this chunk and a margin (to allow overlap)
        List<Crater> craters = new ArrayList<>();
        for (int cx = chunkX - 1; cx <= chunkX + 1; cx++) {
            for (int cz = chunkZ - 1; cz <= chunkZ + 1; cz++) {
                long seed = (cx * 341873128712L + cz * 132897987541L + region.getSeed());
                RandomSource craterRand = RandomSource.create(seed);
                for (int i = 0; i < 2; i++) { // 2 craters per chunk on average
                    if (craterRand.nextDouble() < 0.02) {
                        int centerX = cx * 16 + craterRand.nextInt(16);
                        int centerZ = cz * 16 + craterRand.nextInt(16);
                        // Make max radius a few blocks bigger
                        double radius = 6.0 + craterRand.nextDouble() * 14.0; // was 10.0
                        int depth = 4 + craterRand.nextInt(9);
                        // Randomly choose crater type: true = ice, false = stone
                        boolean isIce = craterRand.nextDouble() < 0.5;
                        craters.add(new Crater(centerX, centerZ, radius, depth, isIce));
                    }
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                // Generate base height using seamless noise
                double noise = generateNoise(worldX, worldZ);
                int baseHeight = Math.round((float)(noise * 20 + 64));

                int height = baseHeight;
                boolean isCraterFloor = false;
                Crater floorCrater = null;
                for (Crater crater : craters) {
                    double dx = worldX - crater.centerX;
                    double dz = worldZ - crater.centerZ;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < crater.radius) {
                        double bowl = Math.cos((dist / crater.radius) * Math.PI) * 0.5 + 0.5;
                        int depression = (int)(bowl * crater.depth);
                        int newHeight = baseHeight - depression;
                        if (newHeight < height) {
                            height = newHeight;
                            if (depression > 0) {
                                isCraterFloor = true;
                                floorCrater = crater;
                            }
                        }
                    }
                }
                for (int y = region.getMinBuildHeight(); y < region.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (y < height) {
                        chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                    } else if (isCraterFloor && y == height) {
                        if (floorCrater != null && !floorCrater.isIce) {
                            // Stone crater floor
                            RandomSource depositRand = RandomSource.create(worldX * 341873128712L + worldZ * 132897987541L + region.getSeed());
                            if (depositRand.nextDouble() < 0.1) { // 10% chance for gravel
                                chunk.setBlockState(pos, Blocks.GRAVEL.defaultBlockState(), false);
                            } else {
                                chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                            }
                            // Place 0-1 mineral deposits at the bottom, randomly scattered
                            if (depositRand.nextDouble() < 0.5) { // 50% chance of one deposit
                                int dx = x + depositRand.nextInt(3) - 1;
                                int dz = z + depositRand.nextInt(3) - 1;
                                if (dx >= 0 && dx < 16 && dz >= 0 && dz < 16) {
                                    BlockPos depositPos = new BlockPos(dx, y, dz);
                                    chunk.setBlockState(depositPos, CustomBlocks.MINERAL_DEPOSIT_BLOCK.get().defaultBlockState(), false);
                                }
                            }
                        } else {
                            // Ice crater floor
                            chunk.setBlockState(pos, Blocks.ICE.defaultBlockState(), false);
                        }
                    } else {
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    // Deterministic, seamless noise for base terrain
    private double generateNoise(int x, int z) {
        double scale = 0.01;
        double nx = x * scale + BASE_TERRAIN_SEED;
        double nz = z * scale + BASE_TERRAIN_SEED;
        double value = 0;
        double amplitude = 1.0;
        double frequency = 1.0;
        for (int i = 0; i < 4; i++) {
            value += amplitude * (Mth.sin((float)(nx * frequency)) * Mth.cos((float)(nz * frequency)));
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        return (value + 1.0) * 0.5;
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
        //Come back to this later
    }

    // Crater class for storing properties
    private static class Crater {
        public final int centerX, centerZ, depth;
        public final double radius;
        public final boolean isIce;
        public Crater(int centerX, int centerZ, double radius, int depth, boolean isIce) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.depth = depth;
            this.isIce = isIce;
        }
    }
} 