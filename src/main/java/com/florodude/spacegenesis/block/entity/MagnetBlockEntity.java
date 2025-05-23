package com.florodude.spacegenesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;

public class MagnetBlockEntity extends BlockEntity {
    private int chargeTicks = 0;
    private BlockPos lastTarget = null;

    public MagnetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGNET_BE.value(), pos, state);
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide) return;
        BlockState state = this.getBlockState();
        Direction facing = state.getValue(com.florodude.spacegenesis.block.MagnetBlock.FACING);
        BlockPos frontPos = this.worldPosition.relative(facing);
        BlockState frontState = level.getBlockState(frontPos);
        boolean isTarget = frontState.getBlock() == com.florodude.spacegenesis.block.CustomBlocks.MINERAL_DEPOSIT_BLOCK.get();

        if (isTarget) {
            // If new target, reset charge
            if (!frontPos.equals(lastTarget)) {
                chargeTicks = 0;
                lastTarget = frontPos;
            }
            // Show stone block particles while charging
            BlockState stoneState = Blocks.STONE.defaultBlockState();
            BlockParticleOption stoneParticle = new BlockParticleOption(ParticleTypes.BLOCK, stoneState);
            ((ServerLevel)level).sendParticles(stoneParticle, 
                frontPos.getX() + 0.5, frontPos.getY() + 0.5, frontPos.getZ() + 0.5, 
                2, 0.2, 0.2, 0.2, 0.01);
            chargeTicks++;
            if (chargeTicks >= 100) {
                // Conversion
                level.setBlockAndUpdate(frontPos, Blocks.GRAVEL.defaultBlockState());
                // Handle iron nugget
                BlockPos backPos = this.worldPosition.relative(facing.getOpposite());
                BlockState backState = level.getBlockState(backPos);
                BlockEntity be = level.getBlockEntity(backPos);
                ItemStack nugget = new ItemStack(Items.IRON_NUGGET);
                if (backState.isAir()) {
                    // Spawn item entity
                    Vec3 spawn = new Vec3(backPos.getX() + 0.5, backPos.getY() + 0.5, backPos.getZ() + 0.5);
                    ItemEntity nuggetEntity = new ItemEntity(level, spawn.x, spawn.y, spawn.z, nugget);
                    level.addFreshEntity(nuggetEntity);
                } else if (be instanceof net.minecraft.world.Container container) {
                    // Try to merge with existing iron nugget stacks first
                    boolean inserted = false;
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack slot = container.getItem(i);
                        if (!slot.isEmpty() && slot.getItem() == Items.IRON_NUGGET && slot.getCount() < slot.getMaxStackSize()) {
                            int space = slot.getMaxStackSize() - slot.getCount();
                            int toAdd = Math.min(space, nugget.getCount());
                            slot.grow(toAdd);
                            nugget.shrink(toAdd);
                            container.setItem(i, slot);
                            if (nugget.isEmpty()) {
                                inserted = true;
                                break;
                            }
                        }
                    }
                    // If not fully inserted, use empty slot(s)
                    if (!inserted && !nugget.isEmpty()) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            if (container.getItem(i).isEmpty()) {
                                container.setItem(i, nugget.copy());
                                nugget.setCount(0);
                                break;
                            }
                        }
                    }
                }
                chargeTicks = 0;
                lastTarget = null;
            }
        } else {
            // No target, reset
            chargeTicks = 0;
            lastTarget = null;
        }
    }
} 