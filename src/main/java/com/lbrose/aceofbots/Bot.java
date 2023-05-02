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
        File[] images = new File[5];
        images[0] = new File("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\ace_of_spades.png");
        images[1] = new File("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\2_of_clubs.png");
        images[2] = new File("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\king_of_hearts2.png");
        images[3] = new File("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\queen_of_spades2.png");
        images[4] = new File("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\10_of_clubs.png");
        ImageMerger merger = new ImageMerger(images);
        merger.mergeImages("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\test.png", 100);

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
