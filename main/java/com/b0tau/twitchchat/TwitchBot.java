package com.b0tau.twitchchat;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.text2speech.Narrator;
import com.sun.media.jfxmedia.logging.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;


import java.io.*;
import java.lang.String;
import java.util.Random;


public class TwitchBot extends Bot{
    private final Random rand = new Random();
    private boolean TTS = false;

    private long TIME_SINCE_SOUND_EVENT = -1L;
    private long TIME_SINCE_SPAWN_EVENT = -1L;
    private long TIME_SINCE_SETTINGS_EVENT = -1L;
    private long TIME_SINCE_WEATHER_EVENT = -1L;

    private int SETTINGS_EVENT_COOLDOWN = TwitchConfig.CLIENT.SETTINGS_EVENT_COOLDOWN.get();
    private int SPAWN_EVENT_COOLDOWN = TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get();
    private int SOUND_EVENT_COOLDOWN = TwitchConfig.CLIENT.SOUND_EVENT_COOLDOWN.get();
    private int WEATHER_EVENT_COOLDOWN = TwitchConfig.CLIENT.SOUND_EVENT_COOLDOWN.get();

    private boolean SOUND_ON = TwitchConfig.CLIENT.SOUND_ON.get();
    private boolean SPAWN_ON = TwitchConfig.CLIENT.SPAWN_ON.get();
    private boolean SETTINGS_ON = TwitchConfig.CLIENT.SETTINGS_ON.get();
    private boolean WEATHER_ON = TwitchConfig.CLIENT.WEATHER_ON.get();

    private final int HOME_SAFE_SPACE = TwitchConfig.CLIENT.HOME_SAFE_SPACE.get();
    private BlockPos HOME_LOCATION = new BlockPos(
            TwitchConfig.CLIENT.HOME_LOCATION_X.get(),
            TwitchConfig.CLIENT.HOME_LOCATION_Y.get(),
            TwitchConfig.CLIENT.HOME_LOCATION_Z.get());

    private final int VERTICAL_SAFE_SPACE = TwitchConfig.CLIENT.VERTICAL_SAFE_SPACE.get();


    public TwitchBot() {
        super(TwitchChat.URL,TwitchChat.PORT, "Streamlabs,b0tau");
    }


    void connect() {
        TwitchChat.LOGGER.debug("PASS oauth:"+TwitchChat.OAUTH+"\r\n");
        try {
            super.getWriter().write("PASS oauth:"+TwitchChat.OAUTH+"\r\n");
            super.getWriter().write("NICK "+TwitchChat.CHANNEL+"\r\n");
            super.getWriter().write("CAP REQ :twitch.tv/membership \r\n");
            super.getWriter().write("CAP REQ :twitch.tv/commands \r\n");
            super.getWriter().write("CAP REQ :twitch.tv/tags \r\n");
            super.getWriter().write("JOIN #" + TwitchChat.CHANNEL + "\r\n");
            super.getWriter().flush();
            TwitchChat.LOGGER.debug("TwitchBot connected!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeCooldown(String msg) {
       //switch("")
    }

    private void chatCommands(String badges, String msg, String username, EntityJoinWorldEvent event, ChatterBot chatterBot) {
        //Checks to make sure event is not on cooldown, prevents unnecessary logic testings
        String[] eventTester = this.checkEventTimers(msg);
        if (Boolean.parseBoolean(eventTester[0]) && !msg.startsWith("toggle")) {
            chatterBot.writeMessage(eventTester[1]);
            return;
        }

        //Mod,Sub, and VIP check
        boolean isMod = checkPermission(badges,"moderator",username);
        boolean isSub = checkPermission(badges,"subscriber",username);
        boolean isVIP = checkPermission(badges,"vip",username);

        if(msg.equals("!home") && username.equals("b0tau")) {
            this.HOME_LOCATION = Minecraft.getInstance().player.getPosition();
            this.writeInGameMessage("Home has been set!");
            TwitchConfig.CLIENT.HOME_LOCATION_X.set((double) this.HOME_LOCATION.getX());
            TwitchConfig.CLIENT.HOME_LOCATION_Y.set((double) this.HOME_LOCATION.getY());
            TwitchConfig.CLIENT.HOME_LOCATION_Z.set((double) this.HOME_LOCATION.getZ());
            TwitchConfig.bakeConfig();
            return;
        }

        //TTS toggle
        if (msg.equals("!tts") && isMod) {
            this.TTS = !this.TTS;
            chatterBot.writeMessage(String.format("TTS has been %s", this.TTS ? "enabled" : "disabled"));
            this.writeInGameMessage(String.format("TTS has been %s", this.TTS ? "enabled" : "disabled"));
            playTTS(msg);
            return;
        }

        //Sound Events
        if (msg.startsWith("!sound ") && TwitchConfig.CLIENT.SOUND_ON.get() || msg.equals("!sound toggle") && isMod) {
            String sound = msg.split(" ")[1];

            switch (sound) {
                case "creeper":
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 60.0F, 1F);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 60.0F, 1F);
                    this.TIME_SINCE_SOUND_EVENT = System.currentTimeMillis() / 1000L;
                    break;
                case "hurt":
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 60.0F, 1F);
                    this.TIME_SINCE_SOUND_EVENT = System.currentTimeMillis() / 1000L;
                    break;
                case "lava":
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 60.0F, 1F);
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_GENERIC_BURN, 60.0F, 1F);

                    this.TIME_SINCE_SOUND_EVENT = System.currentTimeMillis() / 1000L;
                    break;
                case "skeleton":
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 60.0F, 1F);
                    this.TIME_SINCE_SOUND_EVENT = System.currentTimeMillis() / 1000L;
                    break;
                case "toggle":
                    TwitchConfig.CLIENT.SOUND_ON.set(!TwitchConfig.CLIENT.SOUND_ON.get());
                    TwitchConfig.bakeConfig();
                    chatterBot.writeMessage(String.format("%s events have been %s!", "Sound",TwitchConfig.CLIENT.SOUND_ON.get() ? "enabled" : "disabled"));
                    this.writeInGameMessage(String.format("%s events have been %s!", "Sound",TwitchConfig.CLIENT.SOUND_ON.get() ? "enabled" : "disabled"));
                    break;
                default:
                    chatterBot.writeMessage("There is no sound with that name available! Try creeper, hurt, lava, or skeleton instead!");
                    break;
            }
        } else if(!TwitchConfig.CLIENT.SOUND_ON.get() && msg.contains("!client")) {
            chatterBot.writeMessage("Sound events are disabled at the moment!");
        }

        //Settings Events
        if (msg.startsWith("!settings ") && TwitchConfig.CLIENT.SETTINGS_ON.get()|| msg.equals("!settings toggle") && isMod) {
            String[] msg_split = msg.split(" ");
            long currentTime = System.currentTimeMillis() / 1000L;
            switch (msg_split[1]) {
                case "sensitivity":
                    Minecraft.getInstance().gameSettings.mouseSensitivity = Float.parseFloat(msg_split[2]);
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                    break;
                case "auto-jump":
                    Minecraft.getInstance().gameSettings.autoJump = Boolean.parseBoolean(msg_split[2]);
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                    break;
                case "invert-mouse":
                    Minecraft.getInstance().gameSettings.invertMouse = Boolean.parseBoolean(msg_split[2]);
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                    break;
                case "fov":
                    Minecraft.getInstance().gameSettings.fov = Double.parseDouble(msg_split[2]);
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                    break;
                case "hide-gui":
                    Minecraft.getInstance().gameSettings.hideGUI = Boolean.parseBoolean(msg_split[2]);
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                    break;
                case "master-sound":
                    Minecraft.getInstance().gameSettings.setSoundLevel(SoundCategory.MASTER, Float.parseFloat(msg_split[2]));
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                    break;
                case "hostile-volume":
                    Minecraft.getInstance().gameSettings.setSoundLevel(SoundCategory.HOSTILE, Float.parseFloat(msg_split[2]));
                    this.TIME_SINCE_SETTINGS_EVENT = currentTime;
                case "toggle":
                    TwitchConfig.CLIENT.SETTINGS_ON.set(!TwitchConfig.CLIENT.SETTINGS_ON.get());
                    TwitchConfig.bakeConfig();
                    chatterBot.writeMessage(String.format("%s events have been %s!", "Settings",TwitchConfig.CLIENT.SETTINGS_ON.get() ? "enabled" : "disabled"));
                    this.writeInGameMessage(String.format("%s events have been %s!", "Settings",TwitchConfig.CLIENT.SETTINGS_ON.get() ? "enabled" : "disabled"));
                    break;

                default:
                    chatterBot.writeMessage("There is no setting with that name available! " +
                            "Try sensitivity, master-sound, hostile-volume, hide-gui, invert-mouse, or auto-jump instead!");
                    break;
            }
        }  else if(!TwitchConfig.CLIENT.SETTINGS_ON.get() && msg.contains("!ssettings")) {
            chatterBot.writeMessage("Settings events are disabled at the moment!");
        }


        //Spawn Events
        if (msg.startsWith("!spawn ") && TwitchConfig.CLIENT.SPAWN_ON.get() && !checkHomeDistance() || msg.equals("!spawn toggle") && isMod) {
            String[] msg_split = msg.split(" ");
            World world = event.getWorld();
            ClientPlayerEntity player = Minecraft.getInstance().player;
            Vec3d vec = player.getPositionVec();
            if(msg_split[1].equals("creeper") && !isSub
                    || msg_split[1].equals("arrow-volley")  && !isSub
                    || msg_split[1].equals("ghast") && !isSub
                    || msg_split[1].equals("creeper-circle") && !isSub
                    || msg_split[1].equals("zombie-circle") && !isSub) {
                chatterBot.writeMessage("You must be a subscriber to spawn this creature!");

            } else {
                spawnEntity(event, chatterBot, world, msg_split[1], vec);
            }
        } else if (!TwitchConfig.CLIENT.SPAWN_ON.get() && msg.startsWith("!spawn")){
            chatterBot.writeMessage("SPAWN events are disabled at the moment!");
        } else if (checkHomeDistance()) {
            chatterBot.writeMessage(String.format("%s cannot spawn creatures here! Must be %d+ blocks away from their home or not underground more than %d!)",TwitchChat.CHANNEL,this.HOME_SAFE_SPACE,this.VERTICAL_SAFE_SPACE));
        }

        //Weather Events
        if (msg.startsWith("!weather ") && TwitchConfig.CLIENT.WEATHER_ON.get() || msg.equals("!weather toggle") && isMod) {
            String[] msg_split = msg.split(" ");
            int time = rand.nextInt(100000) + 30000;
            switch (msg_split[1]) {
                case "rain":
                    this.setWeather(0,time,0,true,false,event);
                    break;
                case "clear":
                    this.setWeather(time,0,0,false,false,event);
                    break;
                case "thunder":
                    this.setWeather(0,time,time,true,true,event);
                    break;
                case "toggle":
                    TwitchConfig.CLIENT.WEATHER_ON.set(!TwitchConfig.CLIENT.WEATHER_ON.get());
                    TwitchConfig.bakeConfig();
                    chatterBot.writeMessage(String.format("%s events have been %s!", "Weather",TwitchConfig.CLIENT.WEATHER_ON.get() ? "enabled" : "disabled"));
                    this.writeInGameMessage(String.format("%s events have been %s!", "Weather",TwitchConfig.CLIENT.WEATHER_ON.get() ? "enabled" : "disabled"));
                    break;
                default:
                    chatterBot.writeMessage("There is no weather with that name available! Try clear, rain, or thunder instead!");
                    break;
            }
        } else if (!TwitchConfig.CLIENT.WEATHER_ON.get() && msg.startsWith("!weather")) {
            chatterBot.writeMessage("Weather events are disabled at the moment!");
        }


    }

    private String[] checkEventTimers(String msg) {
        TwitchChat.LOGGER.debug(TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get());
        String[] returnArray = new String[2];
        if (msg.startsWith("!setting") && ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_SETTINGS_EVENT) <= this.SETTINGS_EVENT_COOLDOWN && this.TIME_SINCE_SETTINGS_EVENT != -1) {
            returnArray[0] = "true";
            returnArray[1] = String.format("There is %d seconds until you can do another settings event!\n", this.SETTINGS_EVENT_COOLDOWN - ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_SETTINGS_EVENT));
            return returnArray;
        } else if (msg.startsWith("!sound") && ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_SOUND_EVENT) <= this.SOUND_EVENT_COOLDOWN && this.TIME_SINCE_SOUND_EVENT != -1) {
            returnArray[0] = "true";
            returnArray[1] = String.format("There is %d seconds until you can do another sound event!\n", this.SOUND_EVENT_COOLDOWN - ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_SOUND_EVENT));
            return returnArray;
        } else if (msg.startsWith("!spawn") && ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_SPAWN_EVENT) <= TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get() && this.TIME_SINCE_SPAWN_EVENT != -1) {
            returnArray[0] = "true";
            returnArray[1] = String.format("There is %d seconds until you can do another spawn event!\n", (long)TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get() - ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_SPAWN_EVENT));
            return returnArray;
        } else if (msg.startsWith("!weather") && ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_WEATHER_EVENT) <= this.WEATHER_EVENT_COOLDOWN && this.TIME_SINCE_WEATHER_EVENT != -1) {
            returnArray[0] = "true";
            returnArray[1] = String.format("There is %d seconds until you can do another weather event!\n", this.WEATHER_EVENT_COOLDOWN - ((System.currentTimeMillis() / 1000) - this.TIME_SINCE_WEATHER_EVENT));
            return returnArray;
        }
        returnArray[0] = "false";
        return returnArray;
    }

    private boolean checkHomeDistance() {
        if (HOME_LOCATION != null) {
            BlockPos player = Minecraft.getInstance().player.getPosition();
                System.out.printf("HOME_X: %d | HOME_X+SAFTEY: %d || PLAYER_X: %d", HOME_LOCATION.getX(), HOME_LOCATION.getX() + HOME_SAFE_SPACE,player.getX());
            return  player.getX() < TwitchConfig.CLIENT.HOME_LOCATION_X.get() + HOME_SAFE_SPACE
                    && player.getX() > TwitchConfig.CLIENT.HOME_LOCATION_X.get() - HOME_SAFE_SPACE
                    && player.getZ() < TwitchConfig.CLIENT.HOME_LOCATION_Z.get() + HOME_SAFE_SPACE
                    && player.getZ() > TwitchConfig.CLIENT.HOME_LOCATION_Z.get() - HOME_SAFE_SPACE
                    || player.getY() < TwitchConfig.CLIENT.HOME_LOCATION_Y.get() - VERTICAL_SAFE_SPACE;
        } else {
            return false;
        }
    }

    private void checkFollowSubscribe(String msg, String username, EntityJoinWorldEvent event) {
        System.out.printf("username: %s, msg: %s", username, msg);
        if (username.toLowerCase().equals("streamlabs") && msg.contains("following") || msg.contains("subscribed") && username.toLowerCase() == "streamlabs") {
            Minecraft.getInstance().ingameGUI.setOverlayMessage(msg, true);
            fireworkLaunch(event);
            Minecraft.getInstance().ingameGUI.setOverlayMessage(msg, true);

        }
    }

    private boolean checkPermission (String msg, String badge, String username) {
        return msg.contains(badge) || username.equals(TwitchChat.CHANNEL);
    }

    void disconnect() {
        try {
            super.getSocket().close();
            super.getWriter().close();
            super.getWriter().close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void spawnEntity(EntityJoinWorldEvent event, ChatterBot chatterBot, World world, String type, Vec3d vec) {
        Entity entity = null;


        switch(type) {
            case "creeper":
                entity = new CreeperEntity(EntityType.CREEPER, world);
                break;
            case "zombie":
                entity = new ZombieEntity(EntityType.ZOMBIE, world);
                break;
            case "skeleton":
                entity = new SkeletonEntity(EntityType.SKELETON, world);
                break;
            case "spider":
                entity = new SpiderEntity(EntityType.SPIDER, world);
                break;
            case "enderman":
                entity = new EndermanEntity(EntityType.ENDERMAN, world);
                break;
            case "ghast":
                entity = new GhastEntity(EntityType.GHAST, world);
                break;
            case "creeper-circle":
                createEntityCircle(event,4,"creeper",world,0);
                break;
            case "zombie-circle":
                createEntityCircle(event, 4, "zombie",world,0);
                break;
            case "arrow-volley":
                createEntityDisk(event,5,"arrow",world,0);
                break;
            case "toggle":
                this.SPAWN_ON = !this.SPAWN_ON;
                TwitchConfig.CLIENT.SPAWN_ON.set(this.SPAWN_ON);
                TwitchConfig.bakeConfig();
                chatterBot.writeMessage(String.format("%s events have been %s!", "Spawn",this.SPAWN_ON ? "enabled" : "disabled"));
                this.writeInGameMessage(String.format("%s events have been %s!", "Spawn",this.SPAWN_ON ? "enabled" : "disabled"));
                break;
            default:
                chatterBot.writeMessage("There is no entity with that name available! Try creeper, zombie, spider, skeleton, enderman, or ghast instead!");
                return;
        }

        if (entity != null) {
            entity.setPosition(
                    vec.x - 6 + rand.nextInt(12),
                    vec.y,
                    vec.z - 6 + rand.nextInt(12));
            world.addEntity(entity);
            this.TIME_SINCE_SPAWN_EVENT = System.currentTimeMillis() / 1000L;
        }
    }

    private void createEntityDisk(EntityJoinWorldEvent event, int r, String type, World world, int time) {

        int px = Minecraft.getInstance().player.getPosition().getX();
        int py = Minecraft.getInstance().player.getPosition().getY();
        int pz = Minecraft.getInstance().player.getPosition().getZ();

        Entity entity = null;

        for(int x = -r; x <= r; x++) {
            for(int y = r; y >= -r; y--) {
                boolean b = (x) * (x) + (y) * (y) <= r * r;
                if (b) {

                    switch(type) {
                        case "zombie":
                            entity = new ZombieEntity(EntityType.ZOMBIE,world);
                            break;
                        case "firework":
                            entity = new FireworkRocketEntity(world,
                                    px + x, py, pz + y,
                                    new ItemStack(Items.FIREWORK_ROCKET));
                            break;
                        case "creeper":
                            entity = new CreeperEntity(EntityType.CREEPER,world);
                            break;
                        case "arrow":
                            entity = new ArrowEntity(world, px,py,pz);
                            entity.setVelocity(0,2,0);
                            break;
                        default:
                            entity = new EggEntity(
                                    world,
                                    px,py,pz);
                            break;

                    }

                    if (entity != null) {
                        entity.setPosition(px + x, py, pz + y);
                        world.addEntity(entity);

                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    private void createEntityCircle(EntityJoinWorldEvent event, int r, String type, World world, int time) {

        int px = Minecraft.getInstance().player.getPosition().getX();
        int py = Minecraft.getInstance().player.getPosition().getY();
        int pz = Minecraft.getInstance().player.getPosition().getZ();

        Entity entity = null;

        for(int x = -r; x <= r; x++) {
            for(int y = r; y >= -r; y--) {
                boolean b = (x) * (x) + (y) * (y) <= r * r;
                if (b && y == r-x || b && y == -r+x || b && y == r+x || b && y == -r-x) {

                    switch(type) {
                        case "zombie":
                            entity = new ZombieEntity(EntityType.ZOMBIE,world);
                            break;
                        case "firework":
                            entity = new FireworkRocketEntity(world,
                                    px + x, py, pz + y,
                                    new ItemStack(Items.FIREWORK_ROCKET));
                            break;
                        case "creeper":
                            entity = new CreeperEntity(EntityType.CREEPER,world);
                            break;
                        case "arrow":
                            entity = new ArrowEntity(world, px,py,pz);
                            entity.setVelocity(0,2,0);
                            break;
                        default:
                            entity = new EggEntity(
                                    world,
                                    px,py,pz);
                            break;

                    }

                    if (entity != null) {
                        entity.setPosition(px + x, py, pz + y);
                        world.addEntity(entity);

                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    private void playTTS(String msg) {
        if (this.TTS) {
            Narrator.getNarrator().say(msg, false);
        }
    }

    private void fireworkLaunch(EntityJoinWorldEvent event) {
        World world = event.getWorld();

            try {
                createEntityCircle(event, 5, "firework", world, 100);

                EggEntity eggEntity = new EggEntity(
                        world,
                        Minecraft.getInstance().player.getPosition().getX(),
                        Minecraft.getInstance().player.getPosition().getY() + 20,
                        Minecraft.getInstance().player.getPosition().getZ());

                world.addEntity(eggEntity);
            } catch (Exception e) {

            }
    }


    private ChatFormatting getBadgeColor(String s) {
        System.out.println(s);
        if (s.length() < 8) {
            return ChatFormatting.BLUE;
        } else if (s.contains("moderator")) {
            return ChatFormatting.GREEN;
        } else if (s.contains("vip")) {
            return ChatFormatting.LIGHT_PURPLE;
        } else if (s.contains("subscribe")) {
            return ChatFormatting.GOLD;
        } else {
            return ChatFormatting.BLUE;
        }
    }

    public void getChat(EntityJoinWorldEvent event, ChatterBot chatterBot) {
        String line = null;
        try {

            while ((line = super.getReader().readLine()) != null) {
                fireworkLaunch(event);
                String[] str;
                if (line.contains("PRIVMSG")) {

                    TwitchChat.LOGGER.debug(TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get());
                    str = line.split(";");
                    String msg = line.split("PRIVMSG #" + TwitchChat.CHANNEL + " :")[1];
                    String msg_username = str[3].split("display-name=")[1];
                    if (msg.startsWith("!")) {
                        chatCommands(str[1],msg, msg_username, event, chatterBot);
                        continue;
                    }

                    checkFollowSubscribe(msg, msg_username, event);

                    if (!super.getHiddenUsers().toLowerCase().contains(msg_username.toLowerCase()) && msg.substring(0, 1) != "!") {
                        this.writeInGameMessage(getBadgeColor(str[1]) + String.format("%s: ", msg_username) +ChatFormatting.WHITE + String.format("%s", msg));
                    }
                } else if (line.contains("PING")) {
                    super.getWriter().write("PONG\r\n");
                    super.getWriter().flush();
                    chatterBot.writePong();
                }

            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setWeather(int clearWeatherTime, int rainTime, int thunderTime, boolean setRain, boolean setThunder, EntityJoinWorldEvent event) {
        WorldInfo world = event.getWorld().getWorldInfo();
        world.setClearWeatherTime(clearWeatherTime);
        world.setRainTime(rainTime);
        world.setThunderTime(thunderTime);
        world.setRaining(setRain);
        world.setThundering(setThunder);
        this.TIME_SINCE_WEATHER_EVENT = System.currentTimeMillis() / 1000L;
    }

    public void writeInGameMessage(String msg) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().ingameGUI.addChatMessage(ChatType.CHAT,new StringTextComponent(msg));
        }
    }

    public void onDeath(String msg) {

    }

}
