package com.lbrose.poker;

public interface IGame {

    // Updates
    public void updateTotalPot(int totalPot);
    public void updateCommunityCards(Card[] communityCards);
    public void updateRound(Round round);
    public void updatePlayer(Player player);
}
