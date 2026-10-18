package com.blakebr0.mysticalautomation.block;

import com.blakebr0.mysticalautomation.tileentity.MachineShellBlockEntity;
import com.blakebr0.mysticalautomation.tileentity.InfuserBlockEntity;
import com.blakebr0.mysticalautomation.tileentity.CrafterBlockEntity;
import com.blakebr0.mysticalautomation.tileentity.FarmerBlockEntity;
import com.blakebr0.mysticalautomation.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.Containers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Transitional implementation retaining each original machine's blockstate
 * and inventory identity; replaced by individual processors in later phases.
 */
public class MachineShellBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    private final Supplier<? extends BlockEntityType<? extends MachineShellBlockEntity>> machineType;
    private final int slotCount;

    public MachineShellBlock(Supplier<? extends BlockEntityType<? extends MachineShellBlockEntity>> machineType, int slotCount) {
        super(Properties.of().strength(3.5F).sound(SoundType.METAL).requiresCorrectToolForDrops());
        this.machineType = machineType;
        this.slotCount = slotCount;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(RUNNING, false));
    }

    public BlockEntityType<? extends MachineShellBlockEntity> getMachineType() {
        return this.machineType.get();
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (this.machineType.get() == ModBlockEntities.INFUSER.get()) {
            return new InfuserBlockEntity(pos, state);
        }
        if (this.machineType.get() == ModBlockEntities.CRAFTER.get()) {
            return new CrafterBlockEntity(pos, state);
        }
        if (this.machineType.get() == ModBlockEntities.FARMER.get())
            return new FarmerBlockEntity(pos, state);
        return new MachineShellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && type == ModBlockEntities.INFUSER.get()) {
            return createTickerHelper(type, ModBlockEntities.INFUSER.get(), InfuserBlockEntity::tickServer);
        }
        if (!level.isClientSide && type == ModBlockEntities.CRAFTER.get()) {
            return createTickerHelper(type, ModBlockEntities.CRAFTER.get(), CrafterBlockEntity::tickServer);
        }
        if (!level.isClientSide && type == ModBlockEntities.FARMER.get())
            return createTickerHelper(type, ModBlockEntities.FARMER.get(), FarmerBlockEntity::tickServer);
        return null;
    }

    /**
     * Transitional interface pending the upstream-style machine GUI:
     * right-click with crystal/essence/fuel to insert;
     * empty-hand right-click to collect output; sneak + empty-hand to switch target.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CrafterBlockEntity crafter) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                NetworkHooks.openScreen(serverPlayer, crafter, pos);
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof FarmerBlockEntity farmer) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                NetworkHooks.openScreen(serverPlayer, farmer, pos);
            return InteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(pos) instanceof InfuserBlockEntity infuser)) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            infuser.advanceTarget();
            player.displayClientMessage(Component.literal(infuser.getStatus()), true);
            return InteractionResult.CONSUME;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            int inserted = infuser.insertFromPlayer(player, hand);
            if (inserted > 0) {
                player.displayClientMessage(Component.literal("Infuser: inserted " + inserted + " items"), true);
                return InteractionResult.CONSUME;
            }
        }
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, infuser, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, RUNNING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof MachineShellBlockEntity machine && !level.isClientSide) {
                var handler = machine.getInventory();
                for (int i = 0; i < handler.getSlots(); i++) {
                    var stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        handler.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                }
                if (machine instanceof FarmerBlockEntity farmer) {
                    var upgradeStack = farmer.getUpgradeInventory().getStackInSlot(0);
                    if (!upgradeStack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), upgradeStack.copy());
                        farmer.getUpgradeInventory().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                }
                if (machine instanceof CrafterBlockEntity crafter) {
                    var upgrade = crafter.getUpgradeInventory();
                    var stack = upgrade.getStackInSlot(0);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        upgrade.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                }
                if (machine instanceof InfuserBlockEntity infuser) {
                    var upgrade = infuser.getUpgradeInventory();
                    var stack = upgrade.getStackInSlot(0);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        upgrade.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
