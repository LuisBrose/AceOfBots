package com.lbrose.poker;

public interface IGame {

    /** Updates the game info on the client
     * @param data The data to send to the client
     * @param type The type of update to send
     */
    void updateGameInfo(GameStateData data, UpdateType type);

    void showPlayerHand(String id, Card[] hand);
    void requestPlayerMove(String id, int i, boolean b);

    void restartGame();
}
