package br.stm.uno;

import java.util.ArrayList;

class Players extends ArrayList<Player> {

    private int nPlayers;
    private int currentPlayer;
    private boolean clockwiseMotion;

    Players(int nPlayers, DrawPile drawPile) {
        this.nPlayers = nPlayers;
        currentPlayer = 0;
        clockwiseMotion = false;

        for (int i = 0; i < nPlayers; i++) {
            Player player = new Player();
            player.addAll(drawPile.withdraw(7, null));
            add(player);
        }
    }

    Player getCurrentPlayer() {
        return get(currentPlayer);
    }

    int getCurrentIndex() { return currentPlayer; }

    void invertOrder() {
        clockwiseMotion = !clockwiseMotion;
    }

    void nextPlayer() {
        currentPlayer = (clockwiseMotion ? currentPlayer + 1 : currentPlayer + nPlayers - 1) % nPlayers;
    }
}