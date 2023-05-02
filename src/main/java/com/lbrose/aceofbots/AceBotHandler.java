package com.lbrose.aceofbots;

import com.lbrose.poker.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.ArrayList;

public class AceBotHandler implements IGame {
    private Game game = null;
    private String gameMessageId = null;

    public void menu(SlashCommandInteractionEvent event) {
        if (game != null) {
            event.reply("Game already in progress").setEphemeral(true).queue();
            return;
        }
        game = new Game(this);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AceOfBots - Poker")
                .setDescription("A game of texas hold 'em is starting!\n/join to participate")
                .setColor(0x15683f)
                .setThumbnail("https://cdn.discordapp.com/attachments/1096207304946368523/1102299179012857928/74661ed1-54cd-4e3f-9504-1be41e6d3f12.jpg");

        MessageEmbed embed = builder.build();

        InteractionHook table = event.reply("").setEmbeds(embed).setEphemeral(false)
                .addActionRow(
                        Button.success("start", "start"),
                        Button.primary("settings", "settings")
                )
                .complete();
        gameMessageId = table.retrieveOriginal().complete().getId();
    }

    public void startGame(ButtonInteractionEvent event) {
        FileUpload fileUpload = FileUpload.fromData(new File("C:\\Users\\luisb\\OneDrive\\Bilder\\PNG-cards-1.3\\PNG-cards-1.3\\test.png")).setName("output.png");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AceOfBots - Poker")
                .setDescription("River - betting round 4/4 - total pot: 1250 chips - 3 players remaining")
                .setColor(0x15683f)
                .setThumbnail("https://cdn.discordapp.com/attachments/1096207304946368523/1102299179012857928/74661ed1-54cd-4e3f-9504-1be41e6d3f12.jpg")
                .setImage("attachment://" + fileUpload.getName());


        MessageEmbed embed = builder.build();

        Message message = event.getChannel().retrieveMessageById(gameMessageId).complete();
        message.editMessage(" ").setEmbeds(embed).setComponents().setAttachments(fileUpload).queue();
        event.getInteraction().deferEdit().queue();

        game.start();
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

    @Override
    public PlayerStatus getPlayerAction(Player player) {
        return null;
    }

    @Override
    public void updateTotalPot(int totalPot) {

    }

    @Override
    public void updateCommunityCards(Card[] communityCards) {

    }

    @Override
    public void updateRound(Round round) {

    }

    @Override
    public void updatePlayer(Player player) {

    }
}
