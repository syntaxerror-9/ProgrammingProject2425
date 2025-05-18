package it.unibz.inf.pp.clash.logic;


import it.unibz.inf.pp.clash.model.snapshot.Board;

public class Utils {
    public static void PrintBoard(Board board) {
        System.out.println("---Board---");
        for (int i = 0; i <= board.getMaxRowIndex(); i++) {
            for (int j = 0; j <= board.getMaxColumnIndex(); j++) {
                System.out.print(board.getUnit(i, j).isEmpty() ? "0" : "1");
            }
            System.out.println();
        }
    }

}
