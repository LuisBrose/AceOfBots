package com.lbrose.poker;

import java.io.*;
import java.util.Objects;

/**
 * Represents a standard playing card. It contains the suit, rank and value of the card as well as all possible suits and ranks.
 */
public record Card(String suit, String rank, int value) {
    public static final String[] SUITS = {"S", "C", "H", "D"};
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

    public char suitAsIcon() {
        switch (suit) {
            case "S" -> {
                return '♤';
            }
            case "C" -> {
                return '♧';
            }
            case "H" -> {
                return '♥';
            }
            case "D" -> {
                return '♦';
            }
        }
        return ' ';
    }

    public File getAsImage() {
    try {
        InputStream inputStream = getClass().getResourceAsStream("/images/playingCards/" + this + ".png");

        File file = new File(toString());
        OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return file;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
    }

    @Override
    public String toString() {
        return rank + suit;
    }
}
