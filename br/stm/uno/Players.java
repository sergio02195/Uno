package br.stm.uno;

import java.util.ArrayList;

class Players extends ArrayList<Player> {

    private int nPlayers;
    private int currentPlayer;
    private boolean clockwiseMotion;
    private boolean silentMode;

    Players(int nPlayers, DrawPile drawPile,boolean silentMode) {
        this.nPlayers = nPlayers;
        currentPlayer = 0;
        clockwiseMotion = true;
        this.silentMode = silentMode;

        for (int i = 0; i < nPlayers; i++) {
            Player player = new Player();
            player.addAll(drawPile.withdraw(7, null));
            add(player);
        }
    }

    private void Print(String text){
        if(!silentMode) System.out.println(text);
    }

    Player getCurrentPlayer() {
        return get(currentPlayer);
    }

    int getCurrentIndex() { return currentPlayer; }

    void invertOrder() {
        Print("Inverting game order");
        clockwiseMotion = !clockwiseMotion;
    }

    void nextPlayer() {
        currentPlayer = (clockwiseMotion ? currentPlayer + 1 : currentPlayer + nPlayers - 1) % nPlayers;
    }
}
