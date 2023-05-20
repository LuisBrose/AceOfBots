package com.lbrose.aceofbots;

import com.lbrose.mergeImages.ImageMerger;
import com.lbrose.poker.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;

import java.io.File;
import java.util.HashMap;

public class AceBotHandler implements IGame {
    private Game game = null;
    private String gameMessageId = null;
    private MessageChannelUnion channel = null;
    private final HashMap<String, IReplyCallback> playerMenus = new HashMap<>();
    private final HashMap<String, Boolean> playerMenusOnDisplay = new HashMap<>();
    private HashMap<String, Integer> betStorage = new HashMap<>();

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
                        Button.success("join", "join"),
                        Button.primary("start", "start"),
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

    public void addPlayer(IReplyCallback event) {
        if (game == null) {
            event.reply("No game in progress").setEphemeral(true).queue();
            return;
        }

        boolean added = game.addPlayer(event.getUser().getId(), event.getUser().getName());

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

        playerMenusOnDisplay.put(playerId, true);
        playerMenus.get(playerId).getHook().sendMessage("").addEmbeds(embed).addFiles(fileUpload)
                .addActionRow(
                        Button.primary("checkCall", "check/call"),
                        Button.danger("fold", "fold"),
                        Button.success("raise", "raise"),
                        Button.danger("allIn", "all in")
                )
                .queue();
    }

    public void updatePlayerStatus(IMessageEditCallback event, PlayerStatus status) {
        int amount = 0;
        if (status == PlayerStatus.RAISE) amount = betStorage.getOrDefault(event.getUser().getId(), 0);
        game.doPlayerAction(event.getUser().getId(), status, amount);
        event.deferEdit().queue();
    }

    public void openRaiseMenu(ButtonInteractionEvent event) {
        TextInputImpl textInput = new TextInputImpl("textInput", TextInputStyle.SHORT, "change your raise amount:", 1, 8, false, String.valueOf(betStorage.getOrDefault(event.getUser().getId(), 0)), "0");
        Modal modal = Modal.create("modal", "\uD83D\uDCB2↑↓").addComponents(ActionRow.of(textInput)).build();

        event.replyModal(modal).queue();
    }

    public void submitRaise(ModalInteractionEvent event) {
        try {
            betStorage.put(event.getUser().getId(), Integer.valueOf(event.getValue("textInput").getAsString()));
        } catch (Exception e) {
            event.reply("invalid input").setEphemeral(true).queue();
            return;
        }
        updatePlayerStatus(event, PlayerStatus.RAISE);
    }

    @Override
    public void updateGameInfo(GameStateData data, UpdateType type) {
        EmbedBuilder gameInfoBuilder = new EmbedBuilder();
        EmbedBuilder communityBuilder = new EmbedBuilder();

        MessageEmbed gameInfo, communityCards;

        Message message = channel.retrieveMessageById(gameMessageId).complete();

        gameInfoBuilder.setTitle("AceOfBots - Poker")
                .setDescription(data.toString())
                .setColor(0x15683f)
                .setThumbnail("https://cdn.discordapp.com/attachments/1096207304946368523/1102299179012857928/74661ed1-54cd-4e3f-9504-1be41e6d3f12.jpg");

        gameInfo = gameInfoBuilder.build();

        communityBuilder.setTitle("Community Cards:").setColor(0x15683f);

        if (type == UpdateType.ROUND && data.getCommunityCards()!=null) { // if community cards have changed
            File[] images = new File[data.getCommunityCards().length];
            for (int i = 0; i < data.getCommunityCards().length; i++) {
                images[i] = data.getCommunityCards()[i].getAsImage();
            }
            ImageMerger merger = new ImageMerger(images);
            merger.mergeImages("community.png", 100);
            FileUpload fileUpload = FileUpload.fromData(new File("community.png")).setName("community.png");
            communityBuilder.setImage("attachment://" + fileUpload.getName());
            message.editMessage(" ").setEmbeds(gameInfo, communityBuilder.build()).setAttachments(fileUpload).queue();
        } else if(message.getEmbeds().size() > 1 && message.getEmbeds().get(1).getImage() != null) { // if community cards are already displayed
            communityBuilder.setImage(message.getEmbeds().get(1).getImage().getUrl());
        }
        communityCards = communityBuilder.build();

        message.editMessage(" ").setEmbeds(gameInfo, communityCards).queue();
    }

    @Override
    public void showPlayerHand(String id, Card[] hand) {
        if (!playerMenusOnDisplay.getOrDefault(id, false)) showPlayerMenu(id);
    }

    @Override
    public void requestPlayerMove(String id, int i, boolean b) {

    }

    @Override
    public void restartGame() {
        betStorage.clear();
    }
}
