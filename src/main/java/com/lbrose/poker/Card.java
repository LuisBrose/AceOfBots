package com.lbrose.poker;

import java.io.File;

/**
 * Represents a standard playing card. It contains the suit, rank and value of the card as well as all possible suits and ranks.
 */
public record Card(String suit, String rank, int value) {
    public static final String[] SUITS = {"♤", "♧", "♥", "♦"};
    public static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    public static int mapRankToValue(String rank) {
        for (int i = 0; i < RANKS.length; i++) {
            if (RANKS[i].equals(rank)) {
                return i + 2;
            }
        }
        return 0;
    }

    public static boolean containsValue(Card[] cards, int value) {
        for (Card c : cards) {
            if (c.compareValueTo(value) == 0) {
                return true;
            }
        }
        return false;
    }

    private int compareValueTo(int value) {
        return Integer.compare(this.value, value);
    }

    public File getAsImage() {
        return new File("images/playingCards/" + rank + suit + ".png");
    }

    @Override
    public String toString() {
        return rank + suit;
    }
}
