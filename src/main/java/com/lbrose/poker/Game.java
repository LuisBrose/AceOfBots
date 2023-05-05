package com.lbrose.poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Game {
    private final IGame frontEnd;

    private ArrayList<Player> players = new ArrayList<>();
    private Deck deck;
    private Card[] communityCards;
    private int dealer = 0;

    private int totalPot = 0;
    private int currentBet = 0;

    public Game(IGame frontEnd) {
        this.deck = new Deck();
        this.frontEnd = frontEnd;
        Collections.shuffle(players);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Card[] getCommunityCards() {
        return communityCards;
    }

    public int getDealer() {
        return dealer;
    }

    public void resetPlayerStatus() {
        for (Player player : players) {
            PlayerStatus currentStatus = player.getStatus();
            if(currentStatus == PlayerStatus.CALL || currentStatus == PlayerStatus.RAISE || currentStatus == PlayerStatus.CHECK)
                player.setStatus(PlayerStatus.WAITING);
        }
    }

    public void start() {
        deck = new Deck();
        for (Player player : players) {
            player.setHand(deck.drawCard(), deck.drawCard());
        }
        communityCards = deck.getCommunityCards();
        playRound(Round.PREFLOP);
        frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 3));
        playRound(Round.FLOP);
        frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 4));
        playRound(Round.TURN);
        frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 5));
        playRound(Round.RIVER);
        playRound(Round.SHOWDOWN);

    }

    public void playRound(Round round) {
        updateRound(round);
        resetPlayerStatus();
        startBettingRound();
        waitForBettingRound();
    }

    private ArrayList<CompletableFuture<PlayerStatus>> futures = new ArrayList<>();

    public void startBettingRound() {
        boolean bettingFinished = false;

        while (!bettingFinished) {
            bettingFinished = true;
            for (int i = dealer; i < players.size() + dealer; i++) {
                int pIndex = i % players.size();
                Player player = players.get(pIndex);

                PlayerStatus currentStatus = player.getStatus();
                if (currentStatus == PlayerStatus.WAITING){
                    bettingFinished = false;
                    CompletableFuture<PlayerStatus> future = CompletableFuture.supplyAsync(() -> frontEnd.getPlayerAction(player));
                    future.thenAccept(player::setStatus);
                    futures.add(future);
                }
            }
        }
    }

    public void waitForBettingRound() {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
            futures.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nextGame() {
        dealer = (dealer + 1) % players.size();
        start();
    }

    /**
     * Checks if a player has already joined the game
     *
     * @param playerId the id of the player
     * @return true if player is in the game
     */
    public Boolean hasJoined(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) return true;
        }
        return false;
    }

    /**
     * Adds a player to the game
     *
     * @param playerId the id of the player
     * @return true if player was added
     */
    public Boolean addPlayer(String playerId) {
        Player player = new Player(playerId);
        if (players.size() < 8 && !hasJoined(playerId)) return players.add(player);
        return false;
    }

    public void updateRound(Round round) {
        frontEnd.updateRound(round);
    }

}
