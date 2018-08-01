package br.stm.uno;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<Integer> turns = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            Uno uno = new Uno(2);
            while (uno.nextMove()) ;
            turns.add(uno.getTotalTurns());
            Uno clone = uno.cloneGame();


        }
        double media = (double) turns.stream().mapToInt(Integer::intValue).sum() / turns.size();
        System.out.println("Avg: " + media);
    }
}
