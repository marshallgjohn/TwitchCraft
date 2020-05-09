package com.b0tau.twitchchat;


import net.minecraft.client.Minecraft;

import java.io.*;


//Twitch does not allow you to chat when you pull badge, etc info
//Therefore need a separate instance to send user messages in chat via in game chat
public class ChatterBot extends Bot{
    public ChatterBot () {
        super(TwitchChat.URL,TwitchChat.PORT, "Streamlabs,b0tau");
    }

    void connect() {
        TwitchChat.LOGGER.debug(String.format("CHANNEL=%s, OAUTH=%s",TwitchChat.CHANNEL,TwitchChat.OAUTH));
        try {
            super.getWriter().write("PASS oauth:"+TwitchChat.OAUTH+"\r\n");
            super.getWriter().write("NICK "+TwitchChat.CHANNEL+"\r\n");
            super.getWriter().write("JOIN #" + TwitchChat.CHANNEL + "\r\n");
            super.getWriter().flush();
            TwitchChat.LOGGER.debug("chatterBot has connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void writeMessage(String msg) {
        TwitchChat.LOGGER.debug("writeMessage");
        try {
            super.getWriter().write("PRIVMSG #" + TwitchChat.CHANNEL + " :" + msg + "\r\n");
            super.getWriter().flush();
            TwitchChat.LOGGER.debug("ChatterBot.WriteMessage #" +TwitchChat.CHANNEL + ": " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePong() {
        try {
            super.getWriter().write("PONG\r\n");
            super.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
