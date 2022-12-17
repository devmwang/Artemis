/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatComponentMessageAddEvent extends Event {
    private final ChatComponent chat;
    private final Component component;

    public ChatComponentMessageAddEvent(ChatComponent chat, Component component) {
        this.chat = chat;
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public ChatComponent getChat() {
        return chat;
    }
}
