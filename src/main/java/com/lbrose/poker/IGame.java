package com.lbrose.poker;

public interface IGame {

    // Updates
    public void updateTotalPot(int totalPot);
    public void updateCommunityCards(Card[] communityCards);
    public void updateRound(Round round);
    public void updatePlayer(Player player);

    void showPlayerHand(String id, Card[] hand);
    void requestPlayerMove(String id, int i, boolean b);
}
