package com.lbrose.poker;

/**
 * Represents a standard playing card. It contains the suit, rank and value of the card as well as all possible suits and ranks.
 */
public record Card(String suit, String rank, int value) {
    public static final String[] SUITS = {"♠", "♣", "♥", "♦"};
    public static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    @Override
    public String toString() {
        return rank + suit + " (" + value + ")";
    }
}
