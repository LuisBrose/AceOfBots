package com.lbrose.poker;

public class GameStateData {
    private int totalPot;
    private int currentBet;
    private Round round;
    private int numPlayers, numActivePlayers;
    private Card[] communityCards;

    public GameStateData(){
        this.totalPot = 0;
        this.currentBet = 0;
        this.round = Round.PREFLOP;
    }

    public int getTotalPot() {
        return totalPot;
    }

    public void setTotalPot(int totalPot) {
        this.totalPot = totalPot;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public int getNumActivePlayers() {
        return numActivePlayers;
    }

    public void setNumActivePlayers(int numActivePlayers) {
        this.numActivePlayers = numActivePlayers;
    }

    public Card[] getCommunityCards() {
        return communityCards;
    }

    public void setCommunityCards(Card[] communityCards) {
        this.communityCards = communityCards;
    }

    @Override
    public String toString() {
        return "info:\n" +
                "totalPot=" + totalPot +
                "\ncurrentBet=" + currentBet +
                "\nround=" + round;
    }
}
