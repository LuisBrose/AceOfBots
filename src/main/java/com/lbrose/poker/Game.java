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

    /** Checks if a player has already joined the game
     * @param playerId the id of the player
     * @return true if player is in the game
     */
    public Boolean hasJoined(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) return true;
        }
        return false;
    }

    /** Adds a player to the game
     * @param playerId the id of the player
     * @return true if player was added
     */
    public Boolean addPlayer(String playerId) {
        Player player = new Player(playerId);
        if(players.size()<8 && !hasJoined(playerId))return players.add(player);
        return false;
    }

    public void updateRound(String round) {
        bot.updateRound(round);
    }

}
