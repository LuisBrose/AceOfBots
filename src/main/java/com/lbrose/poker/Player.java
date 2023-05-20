package com.lbrose.poker;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class Player {
    private Card[] hand;
    private final String id;
    private final String name;
    private PlayerStatus status;

    private int balance = 1000;
    private int bet = 0;

    public void setHand(Card card1, Card card2) {
        this.hand = new Card[]{card1, card2};
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        setStatus(PlayerStatus.WAITING);
    }

    public String getName() {
        return name;
    }

    public Card[] getHand() {
        return hand;
    }

    public String getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getBet() {
        return bet;
    }

    public int popBet() {
        int temp = bet;
        bet = 0;
        return temp;
    }

    /**Handles the betting logic and PlayerStatus
     * @param tableBet the current highest bet on the table
     * @param raiseAmount the amount the player wants to raise (can be 0 for check or call)
     */
    public void checkCallRaise(int tableBet, int raiseAmount) {
        int callAmount = tableBet + raiseAmount - bet;

        if(callAmount == 0) { // check
            this.status = PlayerStatus.CHECK;
            return;
        }

        if(callAmount > balance) { // force all in
            allIn();
            return;
        }

        if(raiseAmount>0)this.status = PlayerStatus.RAISE; // determine between call and raise
        else this.status = PlayerStatus.CALL;

        this.bet = tableBet+raiseAmount;
        this.balance -= callAmount;
    }

    /** puts all the players money in the pot
     */
    public void allIn() {
        this.status = PlayerStatus.ALL_IN;
        this.bet += balance;
        this.balance = 0;
    }

    /**A double representing the strength of the players hand
     * @param communityCards the cards on the table
     * @return the value of the players hand
     */
    public double getHandValue(Card[] communityCards) {
        Card[] allCards = new Card[7];
        System.arraycopy(hand, 0, allCards, 0, 2);
        System.arraycopy(communityCards, 0, allCards, 2, 5);
        return HandEvaluator.evaluateHand(allCards);
    }

    @Override
    public String toString() {
        return "Player: " + id + " " + Arrays.toString(hand) + " " + status + " " + balance + " " + bet;
    }

}
