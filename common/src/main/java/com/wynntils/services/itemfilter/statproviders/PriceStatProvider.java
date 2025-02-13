/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class PriceStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof GameItem gameItem && gameItem.hasEmeraldPrice()) {
            return List.of(gameItem.getEmeraldPrice());
        }

        return List.of();
    }
}
