/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.utils.MathUtils;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class PowderAnnotator extends GameItemAnnotator {
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("^§[2ebcf8].? ?(Earth|Thunder|Water|Fire|Air) Powder ([IV]{1,3})$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
        Matcher matcher = name.getMatcher(POWDER_PATTERN);
        if (!matcher.matches()) return null;

        Powder element = Powder.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        int tier = MathUtils.integerFromRoman(matcher.group(2));

        PowderTierInfo powderTierInfo = Models.Element.getPowderTierInfo(element, tier);

        return new PowderItem(emeraldPrice, powderTierInfo);
    }
}
