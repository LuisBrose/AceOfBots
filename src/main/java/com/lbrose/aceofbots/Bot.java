package com.lbrose.aceofbots;

import com.lbrose.mergeImages.ImageMerger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.util.EnumSet;

/**
 * Bot
 */
public class Bot {
    public static void main(String[] args) {
        if(System.getenv("BOT_TOKEN") == null) {
            System.out.println("Please set the BOT_TOKEN environment variable");
            System.exit(1);
        }

        EnumSet<GatewayIntent> intents = EnumSet.allOf(GatewayIntent.class); // Intents.ALL

        // Create the JDABuilder instance
        JDA jda = JDABuilder.createDefault(System.getenv("BOT_TOKEN"), intents)
                .setActivity(Activity.competing("/play"))
                .addEventListeners(new AceBotListener())
                .build();

        // Set up slash commands globally
        jda.updateCommands().addCommands(
                Commands.slash("play", "Play a game of poker!"),
                Commands.slash("join", "Join the current game"),
                Commands.slash("leave", "Leave the current game")
        ).queue();
    }
}
