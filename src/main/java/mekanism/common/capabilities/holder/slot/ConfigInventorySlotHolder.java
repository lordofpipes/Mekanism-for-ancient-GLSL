package mekanism.common.capabilities.holder.slot;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import net.minecraft.util.Direction;

public class ConfigInventorySlotHolder extends ConfigHolder<IInventorySlot> implements IInventorySlotHolder {

    ConfigInventorySlotHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addSlot(@Nonnull IInventorySlot slot) {
        slots.add(slot);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ITEM;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> {
            if (slotInfo instanceof InventorySlotInfo && slotInfo.isEnabled()) {
                return ((InventorySlotInfo) slotInfo).getSlots();
            }
            return Collections.emptyList();
        });
    }
}