package com.lbrose.poker;

public final class Card {
    public static final String[] SUITS = {"♠", "♣", "♥", "♦"};
    public static final String[] RANKS = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};

    private final String suit,rank;
    private final int value;

    public Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;

        System.out.println("Card created: " + this.suit + this.rank + " (" + this.value + ")");
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }
}
