package com.lbrose.poker;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class HandEvaluator {
    public static double evaluateHand(Card[] hand) {
        double highestHandValue;
        double checkResult;

        Map<String, Integer> rankFreq = new HashMap<>();
        Map<String, Integer> suitFreq = new HashMap<>();

        for (Card card : hand) {
            String rank = card.rank();
            String suit = card.suit();

            rankFreq.put(rank, rankFreq.getOrDefault(rank, 0) + 1);
            suitFreq.put(suit, suitFreq.getOrDefault(suit, 0) + 1);
        }

        // Check for a Straight
        checkResult = checkStraight(hand);
        highestHandValue = checkResult;

        // Check for a Flush / Straight Flush / Royal Flush
        checkResult = checkSFlush(hand, suitFreq, checkResult);
        highestHandValue = Math.max(highestHandValue, checkResult);
        if (highestHandValue > 9.0) return highestHandValue; // If the hand is a Straight/Royal Flush, return early


        return highestHandValue;
    }

    public static int matchSuitWithFreq(Map<String, Integer> suitFreq, int freq) {
        for (Map.Entry<String, Integer> entry : suitFreq.entrySet()) {
            if (entry.getValue() == freq) {
                return Card.mapSuit(entry.getKey());
            }
        }
        return 0;
    }

    /**
     * Checks if the given hand contains a straight.
     *
     * @param cards The hand to check.
     * @return The value of the straight if it exists, 0.0 otherwise.
     */
    public static double checkStraight(Card[] cards) {
        // Sort the cards by value
        Arrays.sort(cards, Comparator.comparingInt(Card::value));

        // Check for a straight
        int sequenceLength = 1;
        for (int i = 1; i < cards.length; i++) {
            if (cards[i].value() == cards[i - 1].value() + 1) {
                sequenceLength++;
                if (sequenceLength == 5) {
                    return 5.0 + (double) cards[i].value() / 100;
                }
            } else if (cards[i].value() != cards[i - 1].value()) {
                sequenceLength = 1;
            }
        }
        // Check for a wheel straight (A, 2, 3, 4, 5)
        if (Card.containsValue(cards, 14)
                && Card.containsValue(cards, 2)
                && Card.containsValue(cards, 3)
                && Card.containsValue(cards, 4)
                && Card.containsValue(cards, 5)) {
            return 5.0 + (double) 5 / 100;
        }
        return 0.0;
    }

    public static double checkSFlush(Card[] cards, Map<String, Integer> suitFreq, double checkResult) {
        if (suitFreq.containsValue(5) && checkResult != 0.0) { // Straight Flush
            if (checkResult == 5.14) { // Royal Flush
                return 10.0;
            }
            return checkResult + 4.0; // Straight Flush
        }
        for (Map.Entry<String, Integer> entry : suitFreq.entrySet()) {
            if (entry.getValue() == 5) {
                return 6.0 + (double) Card.mapSuit(entry.getKey()) / 100; // to do - check for highest card
            }
        }
        return 0.0;
    }
}
