package br.stm.uno;

public class Main {

    public static void main(String[] args) {
        Uno uno = new Uno(3);
        while(uno.nextMove());
    }
}
