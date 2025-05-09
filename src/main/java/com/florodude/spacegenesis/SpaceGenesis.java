package com.florodude.spacegenesis;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.florodude.spacegenesis.dimension.DimensionSetup;
import net.minecraft.world.level.chunk.ChunkGenerator;
import com.florodude.spacegenesis.dimension.AsteroidChunkGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import com.florodude.spacegenesis.dimension.AsteroidDimension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import org.joml.Matrix4f;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.Camera;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import com.florodude.spacegenesis.dimension.SpaceDimension;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.florodude.spacegenesis.dimension.SpaceChunkGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import com.florodude.spacegenesis.item.CustomItems;
import com.florodude.spacegenesis.block.CustomBlocks;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SpaceGenesis.MODID)
public class SpaceGenesis
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "spacegenesis";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "spacegenesis" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "spacegenesis" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "spacegenesis" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Register the AsteroidChunkGenerator
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = 
        DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);

    // Creates a new Block with the id "spacegenesis:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "spacegenesis:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "spacegenesis:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "spacegenesis:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.spacegenesis")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
                output.accept(com.florodude.spacegenesis.block.CustomBlocks.MINERAL_DEPOSIT_ITEM.get());
                output.accept(com.florodude.spacegenesis.item.CustomItems.FLINT_SCRAPER.get());
            }).build());

    // Register the AsteroidChunkGenerator
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<AsteroidChunkGenerator>> ASTEROID_CHUNK_GENERATOR = CHUNK_GENERATORS.register("asteroid", () -> AsteroidChunkGenerator.CODEC);

    // Register the SpaceChunkGenerator
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<SpaceChunkGenerator>> SPACE_CHUNK_GENERATOR = CHUNK_GENERATORS.register("space", () -> SpaceChunkGenerator.CODEC);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SpaceGenesis(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the SpaceChunkGenerator
        CHUNK_GENERATORS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Initialize dimension setup
        DimensionSetup.init(modEventBus);

        // Register items and blocks
        CustomItems.registerToBus(modEventBus);
        CustomBlocks.registerToBus(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("HELLO FROM CLIENT SETUP");
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Register the /asteroid command
        event.getServer().getCommands().getDispatcher().register(
            Commands.literal("asteroid")
                // No permission required, everyone can use
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel targetLevel = event.getServer().getLevel(AsteroidDimension.ASTEROID_LEVEL);
                    if (targetLevel == null) {
                        context.getSource().sendFailure(Component.literal("Asteroid dimension not found!"));
                        return 0;
                    }
                    // Teleport to spawn or (0, 65, 0)
                    player.teleportTo(targetLevel, 0.5, 65, 0.5, player.getYRot(), player.getXRot());
                    context.getSource().sendSuccess(() -> Component.literal("Teleported to the Asteroid dimension!"), false);
                    return 1;
                })
        );

        // Register the /space command
        event.getServer().getCommands().getDispatcher().register(
            Commands.literal("space")
                // No permission required, everyone can use
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel targetLevel = event.getServer().getLevel(SpaceDimension.SPACE_LEVEL);
                    if (targetLevel == null) {
                        context.getSource().sendFailure(Component.literal("Space dimension not found!"));
                        return 0;
                    }
                    // Teleport to spawn or (0, 65, 0)
                    double x = 0.5, y = 65, z = 0.5;
                    player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
                    // Place a stone block below the player for testing
                    BlockPos below = BlockPos.containing(x, y - 1, z);
                    targetLevel.setBlockAndUpdate(below, Blocks.STONE.defaultBlockState());
                    context.getSource().sendSuccess(() -> Component.literal("Teleported to the Space dimension!"), false);
                    return 1;
                })
        );
    }

    // Handles mod lifecycle events
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            ResourceLocation asteroidEffect = ResourceLocation.parse("spacegenesis:asteroid");
            ResourceLocation spaceEffect = ResourceLocation.parse("spacegenesis:space");
            
            DimensionSpecialEffects customSky = new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false) {
                @Override
                public boolean isFoggyAt(int x, int y) {
                    return false;
                }
                @Override
                public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
                    return Vec3.ZERO;
                }
            };
            event.register(asteroidEffect, customSky);
            event.register(spaceEffect, customSky);
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModCommonEvents {
        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            ServerLevel asteroidLevel = player.server.getLevel(com.florodude.spacegenesis.dimension.AsteroidDimension.ASTEROID_LEVEL);
            if (asteroidLevel != null && player.level().dimension() != com.florodude.spacegenesis.dimension.AsteroidDimension.ASTEROID_LEVEL) {
                player.teleportTo(asteroidLevel, 0.5, 100, 0.5, player.getYRot(), player.getXRot());
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Pre event) {
            Player player = event.getEntity();
            if (player.level().dimension().equals(SpaceDimension.SPACE_LEVEL)) {
                // Apply floating mechanics similar to water
                if (!player.isInWater()) {
                    Vec3 motion = player.getDeltaMovement();
                    boolean isJumping = false;
                    // Only check input on the client side
                    if (player.level().isClientSide && player instanceof net.minecraft.client.player.LocalPlayer clientPlayer) {
                        isJumping = clientPlayer.input != null && clientPlayer.input.jumping;
                    }
                    if (isJumping) {
                        // Simulate underwater upward movement
                        player.setDeltaMovement(motion.x, 0.15, motion.z);
                    } else {
                        // Apply more gradual deceleration in space and slow falling
                        player.setDeltaMovement(
                            motion.x * 0.95,
                            Math.max(motion.y * 0.95, -0.05), // Slow fall, never too fast down
                            motion.z * 0.95
                        );
                    }
                    // Cancel fall damage in space
                    player.fallDistance = 0.0F;
                }
            }
        }
    }
}

