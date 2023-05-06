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
                System.out.println("Player " + playerId + " is " + status);
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
                .join(); // wait for PREFLOP round to finish
        playRound(Round.FLOP)
                .thenRun(() -> frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 4)))
                .join(); // wait for FLOP round to finish
        playRound(Round.TURN)
                .thenRun(() -> frontEnd.updateCommunityCards(Arrays.copyOfRange(communityCards, 0, 5)))
                .join(); // wait for TURN round to finish
        playRound(Round.RIVER)
                .thenRun(() -> playRound(Round.SHOWDOWN))
                .join(); // wait for RIVER and SHOWDOWN rounds to finish
    }

    public CompletableFuture<Void> playRound(Round round) {
        updateRound(round);
        resetPlayerStatus();
        return CompletableFuture.runAsync(this::startBettingRound);
    }

    public void startBettingRound() {
        CountDownLatch latch = new CountDownLatch(players.size());

        for (Player player : players) {
            if (player.getStatus() == PlayerStatus.WAITING) {
                CompletableFuture.runAsync(() -> {
                    synchronized (player) {
                        while (player.getStatus() == PlayerStatus.WAITING) {
                            try {
                                player.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        latch.countDown();
                    }
                });
            } else {
                latch.countDown();
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
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
