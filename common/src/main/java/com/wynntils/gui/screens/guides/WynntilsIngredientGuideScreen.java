/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.gui.screens.WynntilsGuidesListScreen;
import com.wynntils.gui.screens.WynntilsMenuListScreen;
import com.wynntils.gui.screens.guides.widgets.GuideIngredientItemStackButton;
import com.wynntils.gui.widgets.BackButton;
import com.wynntils.gui.widgets.PageSelectorButton;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public final class WynntilsIngredientGuideScreen
        extends WynntilsMenuListScreen<GuideIngredientItemStack, GuideIngredientItemStackButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<GuideIngredientItemStack> allIngredientItems = List.of();

    private WynntilsIngredientGuideScreen() {
        super(Component.translatable("screens.wynntils.wynntilsGuides.ingredientGuide.name"));
    }

    public static Screen create() {
        return new WynntilsIngredientGuideScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsGuidesListScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.ingredientGuide.name"));

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTitle(PoseStack poseStack, String titleString) {
        int txWidth = Texture.QUEST_BOOK_TITLE.width();
        int txHeight = Texture.QUEST_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack, Texture.QUEST_BOOK_TITLE.resource(), 0, 30, 0, txWidth, txHeight, txWidth, txHeight);

        poseStack.pushPose();
        poseStack.translate(10, 36, 0);
        poseStack.scale(1.8f, 1.8f, 0f);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        titleString,
                        0,
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);
        poseStack.popPose();
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (hovered instanceof GuideIngredientItemStackButton guideGearItemStack) {
            GuideIngredientItemStack itemStack = guideGearItemStack.getItemStack();

            List<Component> tooltipLines = itemStack.getTooltipLines(McUtils.player(), TooltipFlag.NORMAL);
            tooltipLines.add(Component.empty());

            String unformattedName = itemStack.getIngredientProfile().getDisplayName();
            if (Managers.Favorites.isFavorite(unformattedName)) {
                tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                        .withStyle(ChatFormatting.GREEN));
            }
            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.open")
                    .withStyle(ChatFormatting.RED));
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    tooltipLines,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    private void renderItemsHeader(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available"),
                        Texture.QUEST_BOOK_BACKGROUND.width() * 0.75f,
                        30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Top,
                        TextShadow.NONE);
    }

    @Override
    protected GuideIngredientItemStackButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuideIngredientItemStackButton(
                xOffset + Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 13, yOffset + 43, 18, 18, elements.get(i), this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(getAllIngredientItems().stream()
                .filter(itemStack ->
                        StringUtils.partialMatch(ComponentUtils.getUnformatted(itemStack.getHoverName()), searchTerm))
                .toList());
    }

    private List<GuideIngredientItemStack> getAllIngredientItems() {
        if (allIngredientItems.isEmpty()) {
            allIngredientItems = Managers.GearProfiles.getIngredientsCollection().stream()
                    .map(GuideIngredientItemStack::new)
                    .toList();
        }

        return allIngredientItems;
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}
