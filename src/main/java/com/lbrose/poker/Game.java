package com.lbrose.poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

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
            if (currentStatus == PlayerStatus.CALL || currentStatus == PlayerStatus.RAISE || currentStatus == PlayerStatus.CHECK)
                player.setStatus(PlayerStatus.WAITING);
        }
    }

    public synchronized Boolean setPlayerStatus(String playerId, PlayerStatus status) {
        for (Player player : players) {
            if (player.getId().equals(playerId) && player.getStatus() == PlayerStatus.WAITING) {
                player.setStatus(status);
                notifyAll();
                return true;
            }
        }
        return false;
    }

    public void start() {
        deck = new Deck();
        for (Player player : players) {
            player.setHand(deck.drawCard(), deck.drawCard());
        }
        communityCards = deck.getCommunityCards();
        playRound(Round.PREFLOP)
                .thenRun(() -> frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 3)))
                .thenCompose(aVoid -> playRound(Round.FLOP))
                .thenRun(() -> frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 4)))
                .thenCompose(aVoid -> playRound(Round.TURN))
                .thenRun(() -> frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 5)))
                .thenCompose(aVoid -> playRound(Round.RIVER))
                .thenRun(() -> playRound(Round.SHOWDOWN));
    }

    public CompletableFuture<Void> playRound(Round round) {
        updateRound(round);
        resetPlayerStatus();
        return CompletableFuture.runAsync(this::startBettingRound);
    }

    public void startBettingRound() {
        CompletableFuture<Void> allPlayersReady = CompletableFuture.allOf(players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.WAITING)
                .map(p -> CompletableFuture.runAsync(() -> {
                    synchronized (p) {
                        while (p.getStatus() == PlayerStatus.WAITING) {
                            try {
                                p.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }))
                .toArray(CompletableFuture[]::new));

        allPlayersReady.join();
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
