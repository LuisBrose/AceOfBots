package com.lbrose.poker;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {
    private final IGame frontEnd;

    private final Hashtable<String, Player> players = new Hashtable<>();
    private Deck deck;
    private Card[] communityCards;

    private int dealer = 0;
    private GameStateData data;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Game(IGame frontEnd) {
        this.deck = new Deck();
        this.frontEnd = frontEnd;
    }

    public Hashtable<String, Player> getPlayers() {
        return players;
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public Card[] getCommunityCards() {
        return communityCards;
    }

    public void resetPlayerStatus() {
        players.values().stream()
                .filter(player -> player.getStatus() == PlayerStatus.CALL || player.getStatus() == PlayerStatus.RAISE || player.getStatus() == PlayerStatus.CHECK)
                .forEach(player -> player.setStatus(PlayerStatus.WAITING));
    }

    /**
     * Makes the next move for a player
     *
     * @param playerId The id of the player
     * @param action   The next move the player should make
     */
    public void doPlayerAction(String playerId, PlayerStatus action, int amount) {
        Player player = players.get(playerId);
        if (player != null && player.getStatus() == PlayerStatus.WAITING) {
            switch (action) {
                case FOLD -> player.setStatus(PlayerStatus.FOLD);
                case CALL, CHECK, RAISE -> player.checkCallRaise(data.getCurrentBet(), amount);
                case ALL_IN -> player.allIn();
                default -> player.setStatus(PlayerStatus.WAITING);
            }
            data.setTotalPot(data.getTotalPot() + player.getBet()); // add the player's bet to the total pot
            data.setCurrentBet(player.getTotalBet()); // update the current bet
            frontEnd.updateGameInfo(data, UpdateType.DEFAULT);

            synchronized (player) {
                player.notifyAll();
                System.out.println(player.getName());
            }
        }
    }

    /**
     * Starts the game and controls the flow of the game
     */
    public void start(boolean newGame) {
        if (newGame) dealer = getRandomDealerIndex();

        threadPool.execute(() -> {
            data = new GameStateData();
            deck = new Deck();
            players.values().forEach(player -> player.setHand(deck.drawCard(), deck.drawCard()));
            communityCards = deck.getCommunityCards();

            players.values().forEach(player -> frontEnd.showPlayerHand(player.getId(), !newGame));

            playRound(Round.PREFLOP)
                    .thenRun(() -> {
                        data.setCommunityCards(Arrays.copyOfRange(communityCards, 0, 3));
                    })
                    .join(); // wait for PRE-FLOP round to finish
            playRound(Round.FLOP)
                    .thenRun(() -> {
                        data.setCommunityCards(Arrays.copyOfRange(communityCards, 0, 4));
                    })
                    .join(); // wait for FLOP round to finish
            playRound(Round.TURN)
                    .thenRun(() -> {
                        data.setCommunityCards(Arrays.copyOfRange(communityCards, 0, 5));
                    })
                    .join(); // wait for TURN round to finish
            playRound(Round.RIVER)
                    .thenRun(() -> {
                        playRound(Round.SHOWDOWN);
                        nextGame();
                    })
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
        data.setRound(round);
        frontEnd.updateGameInfo(data, UpdateType.ROUND);
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
        int numConsecutiveChecks = 0;

        while (numActivePlayers > 0 && numConsecutiveChecks < numActivePlayers) { // !!!CHANGE NUM ACTIVE BACK TO : numActivePlayers > 1
            Player currentPlayer = activePlayers.get(currentPlayerIndex);
            PlayerStatus currentStatus = currentPlayer.getStatus();

            for (Player player : players.values()) {
                frontEnd.updatePlayerInfo(player.getId(), "waiting for other players...", false);
            }

            if (currentStatus == PlayerStatus.WAITING) {
                // Ask the player to make their move asynchronously
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                    // Wait for the player to make their move
                    if (currentPlayer.getStatus() == PlayerStatus.WAITING) {
                        synchronized (currentPlayer) {
                            while (currentPlayer.getStatus() == PlayerStatus.WAITING) {
                                String playerInfo = "make your move: " + (data.getCurrentBet() - currentPlayer.getTotalBet()) + " to call";
                                frontEnd.updatePlayerInfo(currentPlayer.getId(), playerInfo, true);
                                try {
                                    currentPlayer.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                future.join();

                // Process the player's move and update the pot and current bet
                PlayerStatus newStatus = currentPlayer.getStatus();

                if (newStatus == PlayerStatus.FOLD) {
                    // Player has folded, remove them from the active players list
                    activePlayers.remove(currentPlayer);
                    numActivePlayers--;
                } else {
                    if (newStatus == PlayerStatus.RAISE) {
                        // Player has raised, reset the counter for consecutive checks
                        numConsecutiveChecks=0;
                        numConsecutiveChecks++;
                    } else if (newStatus == PlayerStatus.CALL || newStatus == PlayerStatus.CHECK) {
                        // Player has checked, update the counter for consecutive checks
                        numConsecutiveChecks++;
                    }
                }
            }

            // Move on to the next player
            if (numActivePlayers != 0) currentPlayerIndex = (currentPlayerIndex + 1) % numActivePlayers;
            if(currentPlayerIndex == dealer) resetPlayerStatus();
        }
    }

    public Player determineWinner() {
        double max = 0;
        Player winner = null;

        for (Player player : players.values()) {
            if (player.getStatus() != PlayerStatus.FOLD) {
                double strength = player.getHandValue(getCommunityCards());
                if (strength > max) {
                    max = strength;
                    winner = player;
                }
            }
        }
        return winner;
    }

    /**
     * Ends the game and resets the game state
     */
    public void nextGame() {
        dealer = (dealer + 1) % players.size();
        frontEnd.restartGame();
        start(false);
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
    public boolean addPlayer(String playerId, String playerName) {
        Player player = new Player(playerId, playerName);
        return players.size() < 8 && players.putIfAbsent(playerId, player) == null;
    }

    /**
     * Removes a player from the game
     *
     * @param playerId the id of the player
     * @return true if player was removed
     */
    public Boolean removePlayer(String playerId) {
        return players.remove(playerId) != null;
    }

    public int getRandomDealerIndex() {
        return (int) (Math.random() * players.size());
    }
}
