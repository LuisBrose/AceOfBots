package com.lbrose.anotherdiscordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Bot
{
    public static void main( String[] args )
    {
        JDA jda = JDABuilder.createDefault(args[0]).build();
    }
}
