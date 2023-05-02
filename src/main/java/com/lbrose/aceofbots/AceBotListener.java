package com.lbrose.aceofbots;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * AceBotListener
 */
public class AceBotListener extends ListenerAdapter {
    private final AceBotHandler handler; // instance of AceBotHandler to handle events

    public AceBotListener() {
        handler = new AceBotHandler(); // initialize handler
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMessage().getContentRaw().equals("/hi")) {
            event.getChannel().sendMessage("Hi").queue();
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("I'm ready!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "play" -> handler.menu(event);
            case "join" -> handler.addPlayer(event);
            default -> event.reply("Not yet implemented").queue();
        }

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "start" -> handler.startGame(event);
            case "settings" -> event.reply("not implemented yet").queue();
            case "checkCall" -> event.reply("check/call").queue();
            case "raise" -> event.reply("raise").queue();
            case "fold" -> event.reply("fold").queue();
            case "allIn" -> event.reply("all in").queue();
        }
    }
}
