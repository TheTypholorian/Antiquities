package net.hollowed.antique.blocks.entities;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueBlockEntities;
import net.hollowed.antique.networking.PedestalPacketPayload;
import net.hollowed.antique.util.interfaces.ImplementedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class PedestalBlockEntity extends BlockEntity implements WorldlyContainer, ImplementedInventory {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(AntiqueBlockEntities.PEDESTAL_BLOCK_ENTITY, pos, state);
    }

    // Handles dropping the contents when the block is broken
    public void drops() {
        if (level != null) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput view) {
        ContainerHelper.saveAllItems(view, items);
        super.saveAdditional(view);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput view) {
        super.loadAdditional(view);
        ContainerHelper.loadAllItems(view, items);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this.problemPath(), Antiquities.LOGGER);

        CompoundTag var4;
        try {
            TagValueOutput nbtWriteView = TagValueOutput.createWithContext(logging, registries);
            ContainerHelper.saveAllItems(nbtWriteView, items, true);
            var4 = nbtWriteView.buildResult();
        } catch (Throwable var6) {
            try {
                logging.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        logging.close();
        return var4;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level == null) return;
        if (!level.isClientSide() && level instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().blockChanged(worldPosition);
        }
    }

    public static void tick(Level world, PedestalBlockEntity blockEntity) {
        if (world.isClientSide()) {
            return;
        }

        blockEntity.setChanged();
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        setChanged();

        if (level instanceof ServerLevel serverWorld) {
            serverWorld.players().forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, new PedestalPacketPayload(this.worldPosition, stack)));
            serverWorld.getChunkSource().blockChanged(worldPosition);
        }
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(getItems(), slot, count);
        if (!stack.isEmpty()) {
            setChanged();
        }

        setItem(slot, Items.AIR.getDefaultInstance());

        return stack;
    }

    @Override
    public @NotNull int[] getSlotsForFace(@NotNull Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction dir) {
        return items.get(slot).isEmpty() && stack.getCount() > 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction dir) {
        return !items.get(slot).isEmpty();
    }
}
