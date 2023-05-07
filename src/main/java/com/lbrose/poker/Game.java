package com.lbrose.poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {
    private final IGame frontEnd;

    private ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private Deck deck;
    private Card[] communityCards;
    private int dealer = 0;

    private int totalPot = 0;
    private int currentBet = 0;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Game(IGame frontEnd) {
        this.deck = new Deck();
        this.frontEnd = frontEnd;

        List<Player> shuffledPlayers = new ArrayList<>(players.values());
        Collections.shuffle(shuffledPlayers);
        players.replaceAll((id, player) -> shuffledPlayers.remove(0));
    }

    public ConcurrentHashMap<String, Player> getPlayers() {
        return players;
    }

    public Card[] getCommunityCards() {
        return communityCards;
    }

    public int getDealer() {
        return dealer;
    }

    public void resetPlayerStatus() {
        players.values().stream()
                .filter(player -> player.getStatus() == PlayerStatus.CALL || player.getStatus() == PlayerStatus.RAISE || player.getStatus() == PlayerStatus.CHECK)
                .forEach(player -> player.setStatus(PlayerStatus.WAITING));
    }

    /**
     * Sets the status of a player
     *
     * @param playerId The id of the player
     * @param status   The status to set the player to
     * @return True if the player was successfully set to the status, false otherwise
     */
    public Boolean setPlayerStatus(String playerId, PlayerStatus status) {
        Player player = players.get(playerId);
        if (player != null && player.getStatus() == PlayerStatus.WAITING) {
            player.setStatus(status);
            synchronized (player) {
                player.notifyAll();
            }
            return true;
        }
        return false;
    }

    /**
     * Starts the game and controls the flow of the game
     */
    public void start() {
        threadPool.execute(() -> {
            deck = new Deck();
            players.values().forEach(player -> player.setHand(deck.drawCard(), deck.drawCard()));
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
        });
    }

    /**
     * Initiates the next round of the game
     *
     * @param round The round to play
     * @return A CompletableFuture that completes when the round is over
     */
    public CompletableFuture<Void> playRound(Round round) {
        updateRound(round);
        resetPlayerStatus();
        return CompletableFuture.runAsync(this::startBettingRound, threadPool);
    }

    /**
     * Starts a betting round and waits for all players to make their moves
     */
    public void startBettingRound() {
        List<Player> activePlayers = new ArrayList<>(players.values());
        int numActivePlayers = activePlayers.size();

        int currentPlayerIndex = dealer; // The index of the player whose turn it is

        // Variables to keep track of when the betting round is over
        int lastRaiseIndex = currentPlayerIndex;
        int numConsecutiveChecks = 0;

        while (numActivePlayers > 1 && numConsecutiveChecks < numActivePlayers) {
            Player currentPlayer = activePlayers.get(currentPlayerIndex);
            PlayerStatus currentStatus = currentPlayer.getStatus();

            if (currentStatus == PlayerStatus.WAITING || currentStatus == PlayerStatus.CALL || currentStatus == PlayerStatus.RAISE) {
                // Ask the player to make their move asynchronously
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // Ask the front-end to display the player's hand and wait for their move
                    frontEnd.showPlayerHand(currentPlayer.getId(), currentPlayer.getHand());
                    frontEnd.requestPlayerMove(currentPlayer.getId(), currentBet - currentPlayer.getBet(), currentBet == 0);

                    // Wait for the player to make their move
                    synchronized (currentPlayer) {
                        while (currentPlayer.getStatus() == PlayerStatus.WAITING) {
                            try {
                                currentPlayer.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                future.join();

                // Process the player's move and update the pot and current bet
                PlayerStatus newStatus = currentPlayer.getStatus();
                int newBet = currentPlayer.getBet();
                int newPot = totalPot + currentPlayer.getBet();

                if (newStatus == PlayerStatus.FOLD) {
                    // Player has folded, remove them from the active players list
                    activePlayers.remove(currentPlayer);
                    numActivePlayers--;
                } else {
                    // Player has called or raised, update the pot and current bet
                    totalPot = newPot;
                    totalPot += newBet;
                    currentBet = newBet;

                    if (newStatus == PlayerStatus.RAISE) {
                        // Player has raised, reset the counter for consecutive checks
                        lastRaiseIndex = currentPlayerIndex;
                        numConsecutiveChecks = 0;
                    } else if (newStatus == PlayerStatus.CALL) {
                        // Player has checked, update the counter for consecutive checks
                        numConsecutiveChecks++;
                    }
                }
            }

            // Move on to the next player
            currentPlayerIndex = (currentPlayerIndex + 1) % numActivePlayers;
        }
    }

    /**
     * Ends the game and resets the game state
     */
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
    public boolean hasJoined(String playerId) {
        return players.containsKey(playerId);
    }

    /**
     * Adds a player to the game
     *
     * @param playerId the id of the player
     * @return true if player was added
     */
    public boolean addPlayer(String playerId) {
        Player player = new Player(playerId);
        return players.size() < 8 && players.putIfAbsent(playerId, player) == null;
    }

    public void updateRound(Round round) {
        frontEnd.updateRound(round);
    }

}
