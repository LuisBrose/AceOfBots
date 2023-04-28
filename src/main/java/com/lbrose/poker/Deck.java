package com.lbrose.poker;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards; // The cards in the deck
    private final Card[] communityCards; // The 5 cards that are shared between all players

    public Deck() {
        this.cards = new ArrayList<>();

        for (String suit : Card.SUITS) { // Initialize the deck with all 52 cards
            for (int i = 0; i < Card.RANKS.length; i++) {
                cards.add(new Card(suit, Card.RANKS[i], i + 2));
            }
        }

        Collections.shuffle(cards); // Shuffle the deck

        communityCards = new Card[5];
        for (int i = 0; i < 5; i++) { // Draw 5 community cards
            communityCards[i] = drawCard();
        }
    }

    public Card[] getCommunityCards() {
        return communityCards;
    }

    public Card drawCard() {
        return cards.remove(0);
    }

}
