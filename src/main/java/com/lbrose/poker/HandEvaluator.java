package com.lbrose.poker;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that can be used to evaluate the strength of the best hand within seven cards.
 */
public class HandEvaluator {
    /**
     * Returns the highest hand value within the seven cards.
     *
     * @param cards The seven cards to evaluate.
     * @return A double representing the hand value. The higher the score, the better the hand. Ranges from 1.0002 to 10.0.
     */
    public static double evaluateHand(Card[] cards) {
        double highestHandValue;
        double checkResult;

        Map<String, Integer> rankFreq = new HashMap<>();
        Map<String, Integer> suitFreq = new HashMap<>();

        for (Card card : cards) {
            String rank = card.rank();
            String suit = card.suit();

            rankFreq.put(rank, rankFreq.getOrDefault(rank, 0) + 1);
            suitFreq.put(suit, suitFreq.getOrDefault(suit, 0) + 1);
        }

        // Check for a Straight
        checkResult = checkStraight(cards);
        highestHandValue = checkResult;

        // Check for a Flush / Straight Flush / Royal Flush
        checkResult = checkSFlush(cards, suitFreq, checkResult);
        highestHandValue = Math.max(highestHandValue, checkResult);
        if (highestHandValue > 9.0) return highestHandValue; // If the hand is a Straight/Royal Flush, return early

        // Check for a Four of a Kind, Full House, Three of a Kind, Two Pair or Pair otherwise check for high card
        checkResult = checkMultiples(cards, rankFreq);
        highestHandValue = Math.max(highestHandValue, checkResult);

        return highestHandValue;
    }

    /**
     * Can be used to break ties between hands of the same type. (e.g. two players both have a pair of Aces)
     *
     * @param hand The hand to break the tie for.
     * @return A double representing the tiebreaker score. The higher the score, the better the hand. Ranges from 0.0017 to 0.0095.
     */
    public static double tieBreaker(Card[] hand) {
        double tieScore = 0.0;
        for (Card card : hand) {
            tieScore += card.value() / 10000.0;
        }
        return tieScore;
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

    /**
     * Checks if the given hand contains a flush, straight flush or royal flush.
     *
     * @param cards       The hand to check.
     * @param suitFreq    The frequency of each suit in the hand.
     * @param checkResult The value of the straight in the hand.
     * @return The value of the flush, straight flush or royal flush if it exists, 0.0 otherwise.
     */
    public static double checkSFlush(Card[] cards, Map<String, Integer> suitFreq, double checkResult) {
        if (suitFreq.containsValue(5) && checkResult != 0.0) { // Straight Flush
            if (checkResult == 5.14) { // Royal Flush
                return 10.0;
            }
            return checkResult + 4.0; // Straight Flush
        }
        for (Map.Entry<String, Integer> entry : suitFreq.entrySet()) {
            if (entry.getValue() == 5) {
                return 6.0 + tieBreaker(cards); // Flush
            }
        }
        return 0.0;
    }

    /**Gets the value of the rank that occurs the given number of times.
     * @param rankFreq The frequency of each rank in the hand.
     * @param freq The number of times the rank occurs.
     * @param skipFirst Whether to skip the first occurrence of the rank with our desired frequency.
     * @return The value of the rank that occurs the given number of times.
     */
    public static int getRankAsValueFromFreq(Map<String, Integer> rankFreq, int freq, boolean... skipFirst) {
        for (Map.Entry<String, Integer> entry : rankFreq.entrySet()) {
            if (entry.getValue() == freq) {
                if (skipFirst.length > 0 && skipFirst[0]) {
                    skipFirst[0] = false;
                    continue;
                }
                return Card.mapRankToValue(entry.getKey());
            }
        }
        return 0;
    }

    /**Checks if the given hand contains a four of a kind, full house, three of a kind, two pair or pair.
     * @param cards The hand to check.
     * @param rankFreq The frequency of each rank in the hand.
     * @return The value of the four of a kind, full house, three of a kind, two pair or pair if it exists, high card otherwise.
     */
    public static double checkMultiples(Card[] cards, Map<String, Integer> rankFreq) {
        if (rankFreq.containsValue(4)) { // Four of a kind
            return 8.0 + getRankAsValueFromFreq(rankFreq, 4)/100.0;
        }
        if (rankFreq.containsValue(3) && rankFreq.containsValue(2)) { // Full House
            return 7.0 + getRankAsValueFromFreq(rankFreq, 3)/100.0 + getRankAsValueFromFreq(rankFreq, 2)/10000.0;
        }
        if (rankFreq.containsValue(3)) { // Three of a kind
            return 4.0 + getRankAsValueFromFreq(rankFreq, 3)/100.0;
        }
        if (rankFreq.containsValue(2)) { // Two Pair / Pair
            if (getRankAsValueFromFreq(rankFreq, 2, false) != getRankAsValueFromFreq(rankFreq, 2, true)) { // Two Pair
                return 3.0 + Math.max(getRankAsValueFromFreq(rankFreq, 2, false), getRankAsValueFromFreq(rankFreq, 2, true))/100.0;
            }
            else { // Pair
                return 2.0 + getRankAsValueFromFreq(rankFreq, 2)/100.0;
            }
        }
        return 1.0 + tieBreaker(cards);
    }
}
