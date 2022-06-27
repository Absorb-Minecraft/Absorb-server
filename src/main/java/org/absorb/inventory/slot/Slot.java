package org.absorb.inventory.slot;

import org.absorb.inventory.Inventory;
import org.absorb.inventory.item.ItemStack;
import org.spongepowered.math.vector.Vector2i;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;

public interface Slot extends Inventory {

    OptionalInt getIndex();
    
    Optional<Vector2i> getGridPosition();

    Optional<ItemStack> getItem();

    @Override
    default int getSize() {
        return 1;
    }

    @Override
    default Collection<Inventory> getChildren() {
        return Collections.emptyList();
    }

    @Override
    default Collection<Slot> getImmediateSlots() {
        return Collections.emptyList();
    }

    @Override
    default Collection<Slot> getSlots() {
        return Collections.emptyList();
    }
}
