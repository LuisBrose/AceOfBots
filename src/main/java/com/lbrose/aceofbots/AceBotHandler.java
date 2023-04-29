package com.lbrose.aceofbots;

import com.lbrose.poker.Game;
import com.lbrose.poker.Player;
import com.lbrose.poker.PlayerStatus;
import com.lbrose.poker.Round;
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

        event.reply("").setEmbeds(embed).setEphemeral(false)
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

        boolean added = game.addPlayer(event.getUser().getId());

        if (!added) {
            event.reply("already joined or game full").setEphemeral(true).queue();
            return;
        }
        event.reply("Added player " + event.getUser().getName()).setEphemeral(true)
                .addActionRow(
                        Button.primary("checkCall", "check/call"),
                        Button.danger("fold", "fold"),
                        Button.success("raise", "raise"),
                        Button.secondary("allIn", "all in")
                )
                .queue();
    }

    public void updateRound(Round round) {
        //do something
    }

    public PlayerStatus getPlayerAction(Player player)  {
        return PlayerStatus.WAITING;
    }
}
