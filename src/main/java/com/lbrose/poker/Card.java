package com.lbrose.poker;

/**
 * Represents a standard playing card. It contains the suit, rank and value of the card as well as all possible suits and ranks.
 */
public record Card(String suit, String rank, int value) {
    public static final String[] SUITS = {"♠", "♣", "♥", "♦"};
    public static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    public static int mapSuit(String suit) {
        return switch (suit) {
            case "♠" -> 1;
            case "♣" -> 2;
            case "♥" -> 3;
            case "♦" -> 4;
            default -> 0;
        };
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

    @Override
    public String toString() {
        return rank + suit + " (" + value + ")";
    }
}
