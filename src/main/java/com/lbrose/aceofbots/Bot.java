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
        if (args.length == 0) System.exit(456); // No token provided

        EnumSet<GatewayIntent> intents = EnumSet.allOf(GatewayIntent.class); // Intents.ALL

        // Create the JDABuilder instance
        JDA jda = JDABuilder.createDefault(args[0], intents)
                .setActivity(Activity.competing("/play"))
                .addEventListeners(new AceBotListener())
                .build();

        // Set up slash commands globally
        jda.updateCommands().addCommands(
                Commands.slash("play", "Play a game of poker!"),
                Commands.slash("join", "Join the current game")
        ).queue();
    }
}
