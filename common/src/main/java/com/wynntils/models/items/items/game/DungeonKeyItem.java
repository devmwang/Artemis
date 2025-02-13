/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.dungeon.type.Dungeon;
import com.wynntils.models.items.properties.TargetedItemProperty;

public class DungeonKeyItem extends GameItem implements TargetedItemProperty {
    private final Dungeon dungeon;
    private final boolean corrupted;

    public DungeonKeyItem(int emeraldPrice, Dungeon dungeon, boolean corrupted) {
        super(emeraldPrice);
        this.dungeon = dungeon;
        this.corrupted = corrupted;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    @Override
    public String getTarget() {
        return dungeon.getName();
    }

    @Override
    public String toString() {
        return "DungeonKeyItem{" + "dungeon="
                + dungeon + ", corrupted="
                + corrupted + ", emeraldPrice="
                + emeraldPrice + '}';
    }
}
