package br.stm.uno;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<Integer> turns = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Uno uno = new Uno(2, true);
            while (uno.nextMove()) ;
            turns.add(uno.getTotalTurns());
        }
        double media = (double) turns.stream().mapToInt(Integer::intValue).sum() / turns.size();
        System.out.println("Avg: " + media);
    }
}
