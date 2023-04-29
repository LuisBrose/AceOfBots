package com.lbrose.poker;

public class Player {
    private Card[] hand;
    private final String id;
    private PlayerStatus status;
    private int balance;

    public void setHand(Card card1, Card card2) {
        this.hand = new Card[]{card1, card2};
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
}
