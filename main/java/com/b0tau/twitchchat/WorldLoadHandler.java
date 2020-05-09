package com.b0tau.twitchchat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

public class WorldLoadHandler {

    private TwitchBot twitchBot;
    private ChatterBot chatterBot;
    private Thread readChatThread;
    private int count;
    public WorldLoadHandler() {
        this.count = 0;

    }

            //EntityJoinWorldEvent
    @SubscribeEvent
    public void onWordJoin(EntityJoinWorldEvent event) {
        if(this.count < 1) {
            this.twitchBot = new TwitchBot();
            this.chatterBot = new ChatterBot();
            this.twitchBot.connect();
            this.chatterBot.connect();
            new Thread(() -> {
                this.twitchBot.getChat(event,this.chatterBot);
            }).start();

            this.count++;

        }
    }

    @SubscribeEvent
    public void chatEvent (ClientChatEvent event) {
        chatterBot.writeMessage(event.getMessage());
    }


    @SubscribeEvent
    public void onWorldLeave (FMLServerStoppedEvent event) {
        twitchBot.disconnect();
        chatterBot.disconnect();
        this.count = 0;
        TwitchChat.LOGGER.debug("Bots have been disconnected");
    }

    @SubscribeEvent
    public void onGUIOpen (GuiScreenEvent.InitGuiEvent event) {
        final Screen gui = event.getGui();
        TwitchChat.LOGGER.debug(gui);
        if (gui instanceof OptionsScreen) {
            event.addWidget(new Button(gui.width / 2 - 200, gui.height / 4 + 145, 80, 20, "Twitch Config", (x) -> {
                    Minecraft.getInstance().displayGuiScreen(new TwitchConfigScreen());
            }));

        }
    }



}
