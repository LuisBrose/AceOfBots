package com.lbrose.poker;

import com.lbrose.aceofbots.AceBotHandler;

import java.util.ArrayList;

public class Game {
    private ArrayList<Player> players = new ArrayList<>();
    private final AceBotHandler bot;

    private final Deck deck;
    private Card[] communityCards;

    public Game(AceBotHandler botHandler) {
        this.deck = new Deck();
        this.bot = botHandler;
    }

    public void start() {
        for (Player player : players) {
            player.setHand(deck.drawCard(), deck.drawCard());
        }
        communityCards = deck.getCommunityCards();

        updateRound("pre-flop");
        updateRound("flop");
        updateRound("post-flop");
        updateRound("turn");
        updateRound("post-turn");
        updateRound("river");
        updateRound("post-river");

    }

    public Boolean addPlayer(String playerId) {
        Player player = new Player(playerId);
        if(players.size()<8)return players.add(player);
        return false;
    }

    public void updateRound(String round) {
        bot.updateRound(round);
    }

}
