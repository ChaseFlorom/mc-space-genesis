package com.florodude.spacegenesis.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import java.util.Random;

public class MineralDepositBlock extends Block {
    public MineralDepositBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide && tool != null && tool.getItem().getDescriptionId().equals("item.spacegenesis.flint_scraper")) {
            if (new Random().nextFloat() < 0.25f) {
                popResource(level, pos, new ItemStack(Items.IRON_NUGGET));
            }
            // Do NOT call super, so the block itself is not dropped
            return;
        }
        // For all other cases (including by hand), call super to allow loot table drop
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
} 