package com.lbrose.poker;

public interface IGame {
    public PlayerStatus getPlayerAction(Player player);

    // Updates
    public void updateTotalPot(int totalPot);
    public void updateCommunityCards(Card[] communityCards);
    public void updateRound(Round round);
    public void updatePlayer(Player player);
}
