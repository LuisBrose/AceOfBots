package com.lbrose.poker;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    public List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();

        for (String suit : Card.SUITS) {
            for (int i = 0; i < Card.RANKS.length; i++) {
                cards.add(new Card(suit, Card.RANKS[i], i+2));
            }
        }

        Collections.shuffle(cards);
    }


}
