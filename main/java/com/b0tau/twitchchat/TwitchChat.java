package com.b0tau.twitchchat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftGame;
import net.minecraft.world.ITickList;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.World;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

@Mod(TwitchChat.MODID)
public class TwitchChat {

    public static String OAUTH;
    public static String CHANNEL;
    public static final String URL = "irc.twitch.tv";
    public static final int PORT = 6667;
    public static final String MODID  = "twitchchat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private File configFile;

    public TwitchChat() {
        onOpen();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TwitchConfig.CLIENT_SPEC);
        LOGGER.debug("Init TwitchChat");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        MinecraftForge.EVENT_BUS.register(new WorldLoadHandler());



    }

    public void onOpen() {
        try {
            if(new File("config//twitch-config.txt").exists()) {
                BufferedReader reader = new BufferedReader(new FileReader("config//twitch-config.txt"));
                String str;
                CHANNEL = (reader.readLine().split("=")[1]);
                OAUTH = (reader.readLine().split("=")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void preInit(FMLCommonSetupEvent event) {

    }

    public static void setOAUTH(String OAUTH) {
        TwitchChat.OAUTH = OAUTH;
    }


    public static void setCHANNEL(String CHANNEL) {
        TwitchChat.CHANNEL = CHANNEL;
    }





}
