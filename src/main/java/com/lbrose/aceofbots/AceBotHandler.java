package com.lbrose.aceofbots;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AceBotHandler {

    public void play(SlashCommandInteractionEvent event) {
        event.reply("let's go").queue();
    }
}
