/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.ChatComponentMessageAddEvent;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.utils.objects.TimedSet;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@FeatureInfo(category = FeatureCategory.CHAT)
public class ChatMessageRepetitionMergerFeature extends UserFeature {
    // Side effect of using this data structure is that it will only try to merge messages that are sent within 15
    // seconds of each other
    private final TimedSet<ChatMessageInfo> originalToModifiedMessageMap = new TimedSet<>(15, TimeUnit.SECONDS, true);

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceived(ChatMessageReceivedEvent event) {
        saveMessageInfo(event.getOriginalMessage(), event.getMessage());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientsideMessage(ClientsideMessageEvent event) {
        // We put these in the map, but we know these won't get modified
        saveMessageInfo(event.getComponent(), event.getComponent());
    }

    @SubscribeEvent
    public void onChatComponentAddMessage(ChatComponentMessageAddEvent event) {
        Component message = event.getComponent();

        ChatComponent chat = event.getChat();

        for (ChatMessageInfo info : originalToModifiedMessageMap) {
            // If the modified message is the same as the one we're adding
            if (ComponentUtils.equals(info.getModifiedMessage(), message)) {
                // At this point, we know the original message
                // Either it is first time we're seeing it, or it is a message that is duplicated

                // We need to cancel the event either way
                event.setCanceled(true);

                // We need to check if it is duplicated
                if (!chat.allMessages.isEmpty()) {
                    GuiMessage<Component> lastChatMessage = chat.allMessages.get(0);
                    int messageId = info.getId(chat);

                    if (lastChatMessage.getId() == messageId) {
                        info.increaseCount(chat);

                        // This method replaces the last message with this id with the new message
                        chat.addMessage(info.getModifiedMessageWithCount(chat), messageId);

                        return;
                    }
                }

                // If we're here, it means it is the first time we're seeing this message as last in chat
                info.resetAppearanceForChat(chat);
                chat.addMessage(message, info.getId(chat));
                return;
            }
        }

        // If we are here, it means we didn't find the message in the map
        // That should only happen if some mod directly injected a message into the chat
        WynntilsMod.warn("Directly injected message in chat: " + message);
    }

    private void saveMessageInfo(Component original, Component modified) {
        for (ChatMessageInfo info : originalToModifiedMessageMap) {
            // If we already have this message in the set, reset its timer, update to the latest version, and return
            if (ComponentUtils.equals(info.getOriginalMessage(), original)) {
                info.setModifiedMessage(modified);
                originalToModifiedMessageMap.resetTimerFor(info);
                return;
            }
        }

        // Otherwise, add the message to the set
        originalToModifiedMessageMap.put(new ChatMessageInfo(original, modified));
    }

    private static class ChatMessageInfo {
        private final Component originalMessage;
        private Component modifiedMessage;
        private final Map<ChatComponent, Integer> countMap;
        private final Map<ChatComponent, Long> firstAppearanceMap;

        public ChatMessageInfo(Component originalMessage, Component modifiedMessage) {
            this.originalMessage = originalMessage;
            this.modifiedMessage = modifiedMessage;
            this.countMap = new HashMap<>();
            this.firstAppearanceMap = new HashMap<>();
        }

        public Component getOriginalMessage() {
            return originalMessage;
        }

        public Component getModifiedMessage() {
            return modifiedMessage;
        }

        public void setModifiedMessage(Component modifiedMessage) {
            this.modifiedMessage = modifiedMessage;
        }

        public Component getModifiedMessageWithCount(ChatComponent chat) {
            int count = countMap.getOrDefault(chat, 1);
            return modifiedMessage
                    .copy()
                    .append(new TextComponent(" [x%d]".formatted(count)).withStyle(ChatFormatting.GRAY));
        }

        public void increaseCount(ChatComponent chat) {
            // Default value is 1 because calling this would mean a message is already duplicated
            countMap.put(chat, countMap.getOrDefault(chat, 1) + 1);
        }

        public void resetAppearanceForChat(ChatComponent chat) {
            if (countMap.remove(chat) != null) {
                // Reset first appearance timer if we had already seen this message as duplicate before
                firstAppearanceMap.put(chat, System.currentTimeMillis());
            }
        }

        public int getId(ChatComponent chat) {
            firstAppearanceMap.putIfAbsent(chat, System.currentTimeMillis());

            // Generate a hash code based unique id
            return new HashCodeBuilder()
                    .append(ComponentUtils.getCoded(originalMessage))
                    .append(firstAppearanceMap.get(chat))
                    .toHashCode();
        }
    }
}
