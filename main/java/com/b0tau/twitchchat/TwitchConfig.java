package com.b0tau.twitchchat;

import java.io.File;

import net.minecraft.entity.ai.brain.task.ForgetRaidTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = TwitchChat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TwitchConfig {
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static int SETTINGS_EVENT_COOLDOWN;
    public static int SPAWN_EVENT_COOLDOWN;
    public static int SOUND_EVENT_COOLDOWN;
    public static int WEATHER_EVENT_COOLDOWN;

    public static boolean SOUND_ON;
    public static boolean SPAWN_ON;
    public static boolean SETTINGS_ON;
    public static boolean WEATHER_ON;

    public static double HOME_LOCATION_X;
    public static double HOME_LOCATION_Y;
    public static double HOME_LOCATION_Z;

    public static int HOME_SAFE_SPACE;
    public static int VERTICAL_SAFE_SPACE;

/*
    private int HOME_SAFE_SPACE;
    private BlockPos HOME_LOCATION;
    private  int VERTICAL_SAFE_SPACE;*/



    public static void bakeConfig() {
        SETTINGS_EVENT_COOLDOWN = CLIENT.SETTINGS_EVENT_COOLDOWN.get();
        SPAWN_EVENT_COOLDOWN = CLIENT.SPAWN_EVENT_COOLDOWN.get();
        SOUND_EVENT_COOLDOWN = CLIENT.SOUND_EVENT_COOLDOWN.get();
        WEATHER_EVENT_COOLDOWN = CLIENT.WEATHER_EVENT_COOLDOWN.get();
        HOME_LOCATION_X = CLIENT.HOME_LOCATION_X.get();
        HOME_LOCATION_Y = CLIENT.HOME_LOCATION_Y.get();
        HOME_LOCATION_Z = CLIENT.HOME_LOCATION_Z.get();

        HOME_SAFE_SPACE = CLIENT.HOME_SAFE_SPACE.get();
        VERTICAL_SAFE_SPACE = CLIENT.VERTICAL_SAFE_SPACE.get();

        SOUND_ON = CLIENT.SOUND_ON.get();
        SETTINGS_ON = CLIENT.SETTINGS_ON.get();
        SPAWN_ON = CLIENT.SPAWN_ON.get();
        WEATHER_ON = CLIENT.WEATHER_ON.get();


    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == TwitchConfig.CLIENT_SPEC) {
            bakeConfig();
        }
    }

}

class ClientConfig {
    public final ForgeConfigSpec.IntValue SETTINGS_EVENT_COOLDOWN;
    public final ForgeConfigSpec.IntValue SPAWN_EVENT_COOLDOWN;
    public final ForgeConfigSpec.IntValue SOUND_EVENT_COOLDOWN;
    public final ForgeConfigSpec.IntValue WEATHER_EVENT_COOLDOWN;
    public final ForgeConfigSpec.BooleanValue SOUND_ON;
    public final ForgeConfigSpec.BooleanValue SPAWN_ON;
    public final ForgeConfigSpec.BooleanValue SETTINGS_ON;
    public final ForgeConfigSpec.BooleanValue WEATHER_ON;
    public final ForgeConfigSpec.DoubleValue HOME_LOCATION_X;
    public final ForgeConfigSpec.DoubleValue HOME_LOCATION_Y;
    public final ForgeConfigSpec.DoubleValue HOME_LOCATION_Z;
    public final ForgeConfigSpec.IntValue HOME_SAFE_SPACE;
    public final ForgeConfigSpec.IntValue VERTICAL_SAFE_SPACE;


    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("COOLDOWNS");
        SETTINGS_EVENT_COOLDOWN = builder
                .comment("Cooldown before user can submit another settings event in chat!")
                .translation(TwitchChat.MODID + ".config." + "SETTINGS_EVENT_COOLDOWN")
                .defineInRange("SETTINGS_EVENT_COOLDOWN", 60, 0, 1000);
        SPAWN_EVENT_COOLDOWN = builder
                .comment("Cooldown before user can submit another settings event in chat!")
                .translation(TwitchChat.MODID + ".config." + "SPAWN_EVENT_COOLDOWN")
                .defineInRange("SPAWN_EVENT_COOLDOWN", 60, 0, 1000);
        SOUND_EVENT_COOLDOWN = builder
                .comment("Cooldown before user can submit another settings event in chat!")
                .translation(TwitchChat.MODID + ".config." + "SOUND_EVENT_COOLDOWN")
                .defineInRange("SOUND_EVENT_COOLDOWN", 60, 0, 1000);
        WEATHER_EVENT_COOLDOWN = builder
                .comment("Cooldown before user can submit another settings event in chat!")
                .translation(TwitchChat.MODID + ".config." + "WEATHER_EVENT_COOLDOWN")
                .defineInRange("WEATHER_EVENT_COOLDOWN", 60, 0, 1000);
        builder.pop();

        builder.push("EVENTS");
        SOUND_ON = builder
                .comment("Turn sound events on or off")
                .translation(TwitchChat.MODID + ".config." + "SOUND_ON")
                .define("SOUND_ON", true);
        SETTINGS_ON = builder
                .comment("Turn setting events on or off")
                .translation(TwitchChat.MODID + ".config." + "SETTINGS_ON")
                .define("SETTINGS_ON", true);
        SPAWN_ON = builder
                .comment("Turn spawn events on or off")
                .translation(TwitchChat.MODID + ".config." + "SPAWN_ON")
                .define("SPAWN_ON", true);
        WEATHER_ON = builder
                .comment("Turn weather events on or off")
                .translation(TwitchChat.MODID + ".config." + "WEATHER_ON")
                .define("WEATHER_ON", true);
        builder.pop();

        builder.push("HOME_INFO");
        HOME_LOCATION_X = builder
                .comment("If home is set, X value")
                .translation(TwitchChat.MODID + ".config." + "HOME_LOCATION_X")
                .defineInRange("HOME_LOCATION_X",0.0,-999999.0,999999.0);
        HOME_LOCATION_Y = builder
                .comment("If home is set, Y value")
                .translation(TwitchChat.MODID + ".config." + "HOME_LOCATION_Y")
                .defineInRange("HOME_LOCATION_Y",0.0,-999999.0,999999.0);
        HOME_LOCATION_Z = builder
                .comment("If home is set, Z value")
                .translation(TwitchChat.MODID + ".config." + "HOME_LOCATION_Z")
                .defineInRange("HOME_LOCATION_Z",0.0,-999999.0,999999.0);
        HOME_SAFE_SPACE = builder
                .comment("Size of square where enemies cant spawn")
                .translation(TwitchChat.MODID + ".config." + "HOME_SAFE_SPACE")
                .defineInRange("HOME_SAFE_SPACE",30,-99999,99999);
        VERTICAL_SAFE_SPACE = builder
                .comment("Size of square where enemies cant spawn")
                .translation(TwitchChat.MODID + ".config." + "VERTICAL_SAFE_SPACE")
                .defineInRange("VERTICAL_SAFE_SPACE",10,-99999,99999);
        builder.pop();
        builder.push("TWITCH_CONFIG");

    }

}