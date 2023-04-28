package com.lbrose.aceofbots;

import com.lbrose.poker.Game;
import com.lbrose.poker.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class AceBotHandler {
    private Game game = null;

    public void play(SlashCommandInteractionEvent event) {
        game = new Game(this);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AceOfBots")
                .setDescription("A game of poker is starting! /join to join the game.")
                .setColor(Color.green);

        MessageEmbed embed = builder.build();

        event.getChannel().sendMessageEmbeds(embed)
                .addActionRow(
                        Button.success("start", "start"),
                        Button.primary("settings", "settings")
                )
                .queue();
    }

    public void addPlayer(SlashCommandInteractionEvent event) {
        if (game == null) {
            event.reply("No game in progress").queue();
            return;
        }

        String playerId = event.getUser().getId();
        game.addPlayer(playerId);
        event.reply("Added player " + event.getUser().getName())
                .addActionRow(
                        Button.primary("checkCall", "check/call"),
                        Button.success("raise", "raise"),
                        Button.danger("fold", "fold")
                )
                .addActionRow(
                        Button.secondary("allIn", "all in")
                )
                .queue();
    }

    public void updateRound(String round) {
        //do something
    }
}
