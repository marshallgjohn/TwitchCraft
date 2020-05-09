package com.b0tau.twitchchat;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CustomizeSkinScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.OptionSlider;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.AbstractOption;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.text.StringTextComponent;

import java.io.*;

public class TwitchConfigScreen extends SettingsScreen {

    private final Screen screen;
    private final GameSettings settings;
    private TextFieldWidget channel;
    private TextFieldWidget oauth;


    public TwitchConfigScreen() {
        super(Minecraft.getInstance().currentScreen,Minecraft.getInstance().gameSettings, new StringTextComponent("Twitch Config"));
        this.screen = Minecraft.getInstance().currentScreen;
        this.settings = Minecraft.getInstance().gameSettings;



    }

    protected void init() {
        this.addButton(TwitchAbstractOption.SPAWN_COOLDOWN_SLIDER.createWidget(this.settings,this.width/2 - (this.width/5)/2,40,this.width/5));
        this.addButton(TwitchAbstractOption.SOUND_COOLDOWN_SLIDER.createWidget(this.settings,this.width/2 - (this.width/5)/2,65,this.width/5));
        this.addButton(TwitchAbstractOption.SETTINGS_COOLDOWN_SLIDER.createWidget(this.settings,this.width/2 - (this.width/5)/2,90,this.width/5));
        this.addButton(TwitchAbstractOption.WEATHER_COOLDOWN_SLIDER.createWidget(this.settings,this.width/2 - (this.width/5)/2,115,this.width/5));

        this.addButton(TwitchAbstractOption.SPAWN_ON_BOOL.createWidget(this.settings,this.width/2 - this.width/5 - 80,150,80));
        this.addButton(TwitchAbstractOption.SOUND_ON_BOOL.createWidget(this.settings,this.width/2-80,150,80));
        this.addButton(TwitchAbstractOption.SETTINGS_ON_BOOL.createWidget(this.settings,this.width/2 + this.width/5-80,150,80));
        this.addButton(TwitchAbstractOption.WEATHER_ON_BOOL.createWidget(this.settings,this.width/2 + ((this.width/5)*2)-80 ,150,80));

        this.addButton(TwitchAbstractOption.HOME_SAFE_SPACE_SLIDER.createWidget(this.settings,this.width/2 - (this.width/4)-10,185,this.width/4));
        this.addButton(TwitchAbstractOption.VERTICAL_SAFE_SPACE_SLIDER.createWidget(this.settings,this.width/2+10,185,this.width/4));

      this.addButton(new Button(this.width/2-200,this.height/5,100,20,"Bot Settings",(x) ->{
            Minecraft.getInstance().displayGuiScreen(new TwitchBotScreen());
        }));

        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 155, 200, 20, "Done",(x) -> {
           this.removed();
           this.onClose();
        }));

        //this.onOpen();
        //this.t
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 20, 16777215);
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }

    @Override
    public void removed() {
        TwitchConfig.bakeConfig();
    }




}


class TwitchBotScreen extends SettingsScreen {
    private final Screen screen;
    private final GameSettings settings;
    private TextFieldWidget channel;
    private TextFieldWidget oauth;

    public TwitchBotScreen() {
        super(Minecraft.getInstance().currentScreen,Minecraft.getInstance().gameSettings, new StringTextComponent("TwitchChat - Bot Config"));
        this.screen = Minecraft.getInstance().currentScreen;
        this.settings = Minecraft.getInstance().gameSettings;

    }


    public void init() {

        //Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        this.channel = new TextFieldWidget(this.font, this.width / 2-100, 66, 225, 20,"Add Channel");
        //this.channel.func_212954_a(this::func_213028_a);
        channel.setFocused2(true);
        channel.setText(TwitchChat.CHANNEL);
        this.children.add(this.channel);

        this.oauth = new TextFieldWidget(this.font, this.width / 2-100, 120, 225, 20,"OAuth Key");
        //this.channel.func_212954_a(this::func_213028_a);
        oauth.setFocused2(true);
        oauth.setText(TwitchChat.OAUTH);
        oauth.setMaxStringLength(80);
        this.children.add(this.oauth);


        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 155, 200, 20, "Done",(x) -> {
            Minecraft.getInstance().displayGuiScreen(new TwitchConfigScreen());
            this.onClose();
        }));


    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground();
        this.channel.render(p_render_1_,p_render_2_,p_render_3_);
        this.oauth.render(p_render_1_,p_render_2_,p_render_3_);
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 20, 16777215);
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }

    public void onClose() {
        super.onClose();
        TwitchChat.setCHANNEL(this.channel.getText());
        TwitchChat.setOAUTH(this.oauth.getText());
        try {
            FileWriter writer = new FileWriter(new File("config//twitch-config.txt"));
            writer.write(String.format("twitch-channel=%s\n",this.channel.getText()));
            writer.write(String.format("twitch-oauth=%s\n",this.oauth.getText()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

abstract class TwitchAbstractOption extends AbstractOption{

    public TwitchAbstractOption(String translationKeyIn) {
        super(translationKeyIn);
    }

   public static final SliderPercentageOption SPAWN_COOLDOWN_SLIDER = new SliderPercentageOption(
           "Spawn COOLDOWN",
           0,
           1000,
           5F,
           (x) -> {
               return (double)TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get();
           }, (p_216612_0_, p_216612_1_) -> {
               TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.set(p_216612_1_.intValue());
               //TwitchConfig.bakeConfig();
           }, (p_216590_0_, p_216590_1_) -> {
               double d0 = TwitchConfig.CLIENT.SPAWN_EVENT_COOLDOWN.get();
               return "Spawn Cooldown: " + (int)d0;
           });

    public static final SliderPercentageOption SETTINGS_COOLDOWN_SLIDER = new SliderPercentageOption(
            "Spawn COOLDOWN",
            0,
            1000,
            5F,
            (x) -> {
                return (double)TwitchConfig.CLIENT.SETTINGS_EVENT_COOLDOWN.get();
            }, (p_216612_0_, p_216612_1_) -> {
        TwitchConfig.CLIENT.SETTINGS_EVENT_COOLDOWN.set(p_216612_1_.intValue());
        //TwitchConfig.bakeConfig();
    }, (p_216590_0_, p_216590_1_) -> {
        double d0 = TwitchConfig.CLIENT.SETTINGS_EVENT_COOLDOWN.get();
        return "Settings Cooldown: " + (int)d0;
    });

    public static final SliderPercentageOption SOUND_COOLDOWN_SLIDER = new SliderPercentageOption(
            "Sound COOLDOWN",
            0,
            1000,
            5F,
            (x) -> {
                return (double)TwitchConfig.CLIENT.SOUND_EVENT_COOLDOWN.get();
            }, (p_216612_0_, p_216612_1_) -> {
        TwitchConfig.CLIENT.SOUND_EVENT_COOLDOWN.set(p_216612_1_.intValue());
        //TwitchConfig.bakeConfig();
    }, (p_216590_0_, p_216590_1_) -> {
        double d0 = TwitchConfig.CLIENT.SOUND_EVENT_COOLDOWN.get();
        return "Sound Cooldown: " + (int)d0;
    });

    public static final SliderPercentageOption WEATHER_COOLDOWN_SLIDER = new SliderPercentageOption(
            "Weather COOLDOWN",
            0,
            1000,
            5F,
            (x) -> {
                return (double)TwitchConfig.CLIENT.WEATHER_EVENT_COOLDOWN.get();
            }, (p_216612_0_, p_216612_1_) -> {
        TwitchConfig.CLIENT.WEATHER_EVENT_COOLDOWN.set(p_216612_1_.intValue());
        //TwitchConfig.bakeConfig();
    }, (p_216590_0_, p_216590_1_) -> {
        double d0 = TwitchConfig.CLIENT.WEATHER_EVENT_COOLDOWN.get();
        return "Weather Cooldown: " + (int)d0;
    });

    public static final SliderPercentageOption HOME_SAFE_SPACE_SLIDER = new SliderPercentageOption(
            "HOME SAFE SPACE",
            0,
            100,
            1F,
            (x) -> {
                return (double)TwitchConfig.CLIENT.HOME_SAFE_SPACE.get();
            }, (p_216612_0_, p_216612_1_) -> {
        TwitchConfig.CLIENT.HOME_SAFE_SPACE.set(p_216612_1_.intValue());
        //TwitchConfig.bakeConfig();
    }, (p_216590_0_, p_216590_1_) -> {
        double d0 = TwitchConfig.CLIENT.HOME_SAFE_SPACE.get();
        return "Home Safe Space: " + (int)d0;
    });

    public static final SliderPercentageOption VERTICAL_SAFE_SPACE_SLIDER = new SliderPercentageOption(
            "Vertical Safe Space",
            0,
            25,
            1F,
            (x) -> {
                return (double)TwitchConfig.CLIENT.VERTICAL_SAFE_SPACE.get();
            }, (p_216612_0_, p_216612_1_) -> {
        TwitchConfig.CLIENT.VERTICAL_SAFE_SPACE.set(p_216612_1_.intValue());
        //TwitchConfig.bakeConfig();
    }, (p_216590_0_, p_216590_1_) -> {
        double d0 = TwitchConfig.CLIENT.VERTICAL_SAFE_SPACE.get();
        return "Vertical Safe Space: " + (int)d0;
    });

    public static final BooleanOption SPAWN_ON_BOOL = new BooleanOption("SPAWN", (p_216643_0_) -> {
        return TwitchConfig.CLIENT.SPAWN_ON.get();
    }, (p_216656_0_, p_216656_1_) -> {
        TwitchConfig.CLIENT.SPAWN_ON.set(p_216656_1_);
    });
    public static final BooleanOption SETTINGS_ON_BOOL = new BooleanOption("SETTINGS", (p_216643_0_) -> {
        return TwitchConfig.CLIENT.SETTINGS_ON.get();
    }, (p_216656_0_, p_216656_1_) -> {
        TwitchConfig.CLIENT.SETTINGS_ON.set(p_216656_1_);
    });
    public static final BooleanOption SOUND_ON_BOOL = new BooleanOption("SOUND", (p_216643_0_) -> {
        return TwitchConfig.CLIENT.SOUND_ON.get();
    }, (p_216656_0_, p_216656_1_) -> {
        TwitchConfig.CLIENT.SOUND_ON.set(p_216656_1_);
    });
    public static final BooleanOption WEATHER_ON_BOOL = new BooleanOption("WEATHER", (p_216643_0_) -> {
        return TwitchConfig.CLIENT.WEATHER_ON.get();
    }, (p_216656_0_, p_216656_1_) -> {
        TwitchConfig.CLIENT.WEATHER_ON.set(p_216656_1_);
    });
}




/*class TwitchScreen() extends {

}*/
