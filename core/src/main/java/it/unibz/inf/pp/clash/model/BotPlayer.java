package it.unibz.inf.pp.clash.model;

import it.unibz.inf.pp.clash.model.impl.GameEventHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.EmptyStackException;
import java.util.Random;

public class BotPlayer {

    // How much the bot has to wait to play the nextMove
    public static int BOT_MOVE_DELAY = 1000;

    interface Move {
        void perform(GameSnapshot gameSnapshot);
    }

    record MoveUnit(int from, int to) implements Move {
        @Override
        public void perform(GameSnapshot gameSnapshot) {
            var handler = GameEventHandler.getInstance();
            var currentBoard = gameSnapshot.getCurrentBoard();
            var boardStacks = currentBoard.getNormalizedBoard();
            handler.selectTile(currentBoard.getRealRowIndex(boardStacks[from].size() - 1), from);
            handler.selectTile(currentBoard.getRealRowIndex(boardStacks[to].size()), to);
        }
    }

    record DeleteUnit(int from) implements Move {
        @Override
        public void perform(GameSnapshot gameSnapshot) {

        }
    }

    record CallReinforcement() implements Move {
        @Override
        public void perform(GameSnapshot gameSnapshot) {
            var handler = GameEventHandler.getInstance();
            handler.callReinforcement();
        }
    }


    private static Move chooseMove(GameSnapshot gs) {
        // First step: look for possible combinations of attacking. Then look for combinations of defensive move.
        // If the first step fails, Second step: look for deletion spots
        // If the second step fails, Third Step: call Reinforcement

        var botBoard = gs.getCurrentBoard();
        var boardStacks = botBoard.getNormalizedBoard();

        for (int i = 0; i < boardStacks.length; i++) {
            Unit leftUnit;
            try {
                leftUnit = boardStacks[i].peek();
            } catch (EmptyStackException ex) {
                continue;
            }
            if (!(leftUnit instanceof MobileUnit mobileLeftUnit)) continue;
            for (int j = 0; j < boardStacks.length; j++) {
                if (i == j) continue;
                var rightUnits = boardStacks[j];
                // Check if column is full
                if ((rightUnits.size() - 1) == botBoard.getMaxRowIndex()) continue;
                // There cant be an attack formation if only 1 unit is in the column
                if (rightUnits.size() <= 1) continue;

                var topRightUnit = rightUnits.get(rightUnits.size() - 1);
                var oneBeforeTopRightUnit = rightUnits.get(rightUnits.size() - 2);

                if (!(topRightUnit instanceof MobileUnit mTopRightUnit) || !(oneBeforeTopRightUnit instanceof MobileUnit mOneBeforeTopRightUnit))
                    continue;

                if (mobileLeftUnit.getClass() == mTopRightUnit.getClass() && mobileLeftUnit.getClass() == mOneBeforeTopRightUnit.getClass()
                        && mobileLeftUnit.getColor().equals(mTopRightUnit.getColor()) && mobileLeftUnit.getColor().equals(mOneBeforeTopRightUnit.getColor())
                ) {
                    return new MoveUnit(i, j);
                }
                // TODO: Check for defensive move
            }
        }
        return new CallReinforcement();
    }

    public static void PlayMove(GameSnapshot gs) {
        Move move = chooseMove(gs);
        move.perform(gs);
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
