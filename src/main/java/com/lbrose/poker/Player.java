package com.lbrose.poker;

public class Player {
    private Card[] hand;
    private final String id;

    public Player(String id) {
        this.id = id;
    }

    public void setHand(Card card1, Card card2) {
        this.hand = new Card[]{card1, card2};
    }

    public Card[] getHand() {
        return hand;
    }

    public String getId() {
        return id;
    }
}
