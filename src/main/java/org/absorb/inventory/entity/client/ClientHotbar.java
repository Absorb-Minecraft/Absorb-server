package org.absorb.inventory.entity.client;

import org.absorb.inventory.Inventory;
import org.absorb.inventory.InventoryType;
import org.absorb.inventory.grid.GridInventory;
import org.absorb.inventory.slot.AbstractSlot;
import org.absorb.inventory.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector2i;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class ClientHotbar implements GridInventory {

    private final Slot[] slots;
    private byte selected;
    private final ClientInventory parent;

    public ClientHotbar(@NotNull ClientInventory parent) {
        this.parent = parent;
        this.slots = new Slot[9];
        for (int i = 0; i < 9; i++) {
            this.slots[i] = new AbstractSlot(36 + i, parent, null, new Vector2i(i, 0));
        }
    }

    public byte getSelected() {
        return this.selected;
    }

    public ClientHotbar setSelected(byte selected) {
        this.selected = selected;
        return this;
    }

    @Override
    public int getSize() {
        return 9;
    }

    @Override
    public Collection<Inventory> getChildren() {
        return Arrays.asList(this.slots);
    }

    @Override
    public Optional<Inventory> getParent() {
        return Optional.of(this.parent);
    }

    @Override
    public Collection<Slot> getImmediateSlots() {
        return Arrays.asList(this.slots);
    }

    @Override
    public Optional<InventoryType> getType() {
        return Optional.empty();
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public Slot getSlot(int x, int y) {
        if (x!=0) {
            throw new IndexOutOfBoundsException("X cannot be anything other then 0");
        }
        return this.slots[y];
    }
}
