package com.lbrose.poker;

public interface IGame {

    /** Updates the game info on the client
     * @param data The data to send to the client
     * @param type The type of update to send
     */
    void updateGameInfo(GameStateData data, UpdateType type);
    void updatePlayerInfo(String playerId, String info);

    void showPlayerHand(String id, Card[] hand);
    void restartGame();
}
