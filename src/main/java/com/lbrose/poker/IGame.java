package com.lbrose.poker;

public interface IGame {

    // Updates
    public void updateGameInfo(GameStateData data);

    void showPlayerHand(String id, Card[] hand);
    void requestPlayerMove(String id, int i, boolean b);

    void restartGame();
}
