package com.lbrose.aceofbots;

import com.lbrose.mergeImages.ImageMerger;
import com.lbrose.poker.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.HashMap;

public class AceBotHandler implements IGame {
    private Game game = null;
    private String gameMessageId = null;
    private MessageChannelUnion channel = null;
    private HashMap<String, SlashCommandInteractionEvent> playerMenus = new HashMap<>();

    public void gameMenu(SlashCommandInteractionEvent event) {
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
        channel = event.getChannel();
        gameMessageId = table.retrieveOriginal().complete().getId();
    }

    public void startGame(ButtonInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AceOfBots - Poker")
                .setDescription("")
                .setColor(0x15683f)
                .setThumbnail("https://cdn.discordapp.com/attachments/1096207304946368523/1102299179012857928/74661ed1-54cd-4e3f-9504-1be41e6d3f12.jpg");


        MessageEmbed embed = builder.build();

        Message message = event.getChannel().retrieveMessageById(gameMessageId).complete();
        message.editMessage(" ").setEmbeds(embed).setComponents().queue();
        event.getInteraction().deferEdit().queue();

        game.start();
    }

    public void addPlayer(SlashCommandInteractionEvent event) {
        if (game == null) {
            event.reply("No game in progress").setEphemeral(true).queue();
            return;
        }

        boolean added = game.addPlayer(event.getUser().getId());

        if (!added) {
            event.reply("already joined or game full").setEphemeral(true).queue();
            return;
        }

        playerMenus.put(event.getUser().getId(), event);
        event.deferReply(true).queue();
    }

    public void removePlayer(SlashCommandInteractionEvent event) {
        if (game == null) {
            event.reply("No game in progress").queue();
            return;
        }

        boolean removed = game.removePlayer(event.getUser().getId());

        if (!removed) {
            event.reply("not joined").setEphemeral(true).queue();
            return;
        }
        event.reply("you left the game").setEphemeral(true).queue();
    }

    public void showPlayerMenu(String playerId) {
        Card[] playerHand = game.getPlayer(playerId).getHand();

        File[] images = new File[playerHand.length];
        for (int i = 0; i < playerHand.length; i++) {
            images[i] = playerHand[i].getAsImage();
        }
        ImageMerger merger = new ImageMerger(images);
        merger.mergeImages(playerId + ".png", 100);

        FileUpload fileUpload = FileUpload.fromData(new File(playerId + ".png")).setName(playerId + ".png");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(" ")
                .setColor(0x15683f)
                .setImage("attachment://" + fileUpload.getName());

        MessageEmbed embed = builder.build();

        playerMenus.get(playerId).getHook().sendMessage("").addEmbeds(embed).addFiles(fileUpload)
                .addActionRow(
                        Button.primary("checkCall", "check/call"),
                        Button.danger("fold", "fold"),
                        Button.success("raise", "raise"),
                        Button.secondary("allIn", "all in")
                ).queue();
    }

    public void updatePlayerStatus(ButtonInteractionEvent event, PlayerStatus status) {
        int amount = 0;
        if (status == PlayerStatus.RAISE) amount = 0; //insert raise amount later !!!
        game.doPlayerAction(event.getUser().getId(), status,amount);
        event.getInteraction().deferEdit().queue();
    }

    @Override
    public void updateTotalPot(int totalPot) {

    }

    @Override
    public void updateCommunityCards(Card[] communityCards) {
        File[] images = new File[communityCards.length];
        for (int i = 0; i < communityCards.length; i++) {
            images[i] = communityCards[i].getAsImage();
        }
        ImageMerger merger = new ImageMerger(images);
        merger.mergeImages("community.png", 100);

        FileUpload fileUpload = FileUpload.fromData(new File("community.png")).setName("community.png");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AceOfBots - Poker")
                .setDescription("River - betting round 4/4 - total pot: 1250 chips - 3 players remaining")
                .setColor(0x15683f)
                .setThumbnail("https://cdn.discordapp.com/attachments/1096207304946368523/1102299179012857928/74661ed1-54cd-4e3f-9504-1be41e6d3f12.jpg")
                .setImage("attachment://" + fileUpload.getName());

        MessageEmbed embed = builder.build();

        Message message = channel.retrieveMessageById(gameMessageId).complete();
        message.editMessage(" ").setEmbeds(embed).setComponents().setAttachments(fileUpload).queue();
    }

    @Override
    public void updateRound(Round round) {

    }

    @Override
    public void updatePlayer(Player player) {

    }

    @Override
    public void showPlayerHand(String id, Card[] hand) {
        showPlayerMenu(id);
    }

    @Override
    public void requestPlayerMove(String id, int i, boolean b) {

    }
}
