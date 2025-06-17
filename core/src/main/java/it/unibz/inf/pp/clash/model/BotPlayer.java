package it.unibz.inf.pp.clash.model;

import it.unibz.inf.pp.clash.model.impl.GameEventHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;

import java.util.Random;

public class BotPlayer {
    interface Move {
        void perform();
    }

    record MoveUnit(int from, int to) implements Move {
    }

    record DeleteUnit(int from) {
    }

    record CallReinforcement() {
    }

    enum MoveType {
        MoveUnit,
        DeleteUnit,
        CallReinforcement
    }

    private MoveType chooseMove(GameSnapshot gs) {

    }

    public static void PlayMove(GameSnapshot gs) {
        var rng = new Random();
        MoveType moveType = MoveType.values()[rng.nextInt(MoveType.values().length)];
        moveType = MoveType.MoveUnit;
        var currentBoard = gs.getCurrentBoard();
        var handler = GameEventHandler.getInstance();
        switch (moveType) {
            case MoveUnit -> {
                MoveUnit(0, 1, currentBoard);
            }
            case DeleteUnit -> {

//                handler.deleteUnit(currentBoard.);
            }
            case CallReinforcement -> {
                handler.callReinforcement();
            }
        }
        try {
            // Sleep a bit so that the play can see the move
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void MoveUnit(int columnFrom, int columnTo, NormalizedBoard board) {
        var boardStacks = board.getNormalizedBoard();
        var rowFromLast = boardStacks[columnFrom].size() - 1;
        var rowToTop = boardStacks[columnFrom].size();

        var gameHandler = GameEventHandler.getInstance();
        gameHandler.selectTile(board.getRealRowIndex(rowFromLast), columnFrom);
        gameHandler.selectTile(board.getRealRowIndex(rowToTop), columnTo);
    }
}
