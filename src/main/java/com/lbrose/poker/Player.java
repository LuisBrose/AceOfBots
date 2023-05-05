package com.lbrose.poker;

import java.util.concurrent.CompletableFuture;

public class Player {
    private Card[] hand;
    private final String id;
    private PlayerStatus status;

    private int balance;
    private int bet;

    public void setHand(Card card1, Card card2) {
        this.hand = new Card[]{card1, card2};
    }

    public CompletableFuture<PlayerStatus> getFuture() {
        CompletableFuture<PlayerStatus> future = new CompletableFuture<>();
        return future;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public Player(String id) {
        this.id = id;
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

    public int callRaise(int tableBet, int raiseAmount) {
        int callAmount = tableBet + raiseAmount - bet;
        int maxCallAmount = balance - callAmount;

        if(maxCallAmount < 0) {
            this.status = PlayerStatus.ALL_IN;
            this.bet += balance;
            this.balance = 0;
            return tableBet;
        }

        this.status = PlayerStatus.CALL;
        this.bet = tableBet+raiseAmount;
        this.balance -= callAmount;
        return bet;
    }

    public int fold() {
        this.status = PlayerStatus.FOLD;
        int lostBet = bet;
        bet = 0;
        return lostBet;
    }

    public int check() {
        this.status = PlayerStatus.CHECK;
        return bet;
    }

    public double getHandValue(Card[] communityCards) {
        Card[] allCards = new Card[7];
        System.arraycopy(hand, 0, allCards, 0, 2);
        System.arraycopy(communityCards, 0, allCards, 2, 5);
        return HandEvaluator.evaluateHand(allCards);
    }

}
