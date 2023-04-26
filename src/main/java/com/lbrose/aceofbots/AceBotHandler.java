package com.lbrose.aceofbots;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class AceBotHandler {

    public void play(SlashCommandInteractionEvent event) {
        event.reply("let's go").queue();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AceOfBots")
                .setColor(Color.green);

        MessageEmbed embed = builder.build();

        event.getChannel().sendMessageEmbeds(embed)
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
}
