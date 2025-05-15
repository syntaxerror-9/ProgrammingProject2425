package it.unibz.inf.pp.clash.logic;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;

import java.util.*;

public class GameSnapshotUtils {


    //random butterflies
    public static void placeRandomButterflies(GameSnapshot gs, int count, int[] targetRows) {
        List<Board.TileCoordinates> validTiles = new ArrayList<>();

        for (int row : targetRows) {
            for (int col = 0; col <= gs.getBoard().getMaxColumnIndex(); col++) {
                if (gs.getBoard().getUnit(row, col).isEmpty()) {
                    validTiles.add(new Board.TileCoordinates(row, col));
                }
            }
        }

        Collections.shuffle(validTiles);
        Random random = new Random();

        int placed = 0;
        while (placed < count && !validTiles.isEmpty()) {
            Board.TileCoordinates pos = validTiles.remove(0);
            UnitColor color = UnitColor.values()[random.nextInt(3)];
            gs.getBoard().addUnit(pos.rowIndex(), pos.columnIndex(), new Butterfly(color));
            placed++;
        }
    }


    //consuming actions
    public static void consumeAction(GameSnapshot gs, DisplayManager displayManager) {
        gs.decrementActions();
        int remaining = gs.getNumberOfRemainingActions();
        if (remaining <= 0) {
            gs.switchTurn();
            displayManager.drawSnapshot(gs, "Turn ended. Now it's " + gs.getActivePlayer() + "'s turn.");
        } else {
            displayManager.drawSnapshot(gs, "Action performed. Remaining: " + remaining);
        }
    }


    //checking who can interact with what (no cheating!!!)
    public static boolean isTileOwnedByActivePlayer(Object snapshot, int rowIndex, DisplayManager displayManager) {
        if (!(snapshot instanceof GameSnapshot gs)) return false;

        boolean isValid = switch (gs.getActivePlayer()) {
            case FIRST -> rowIndex >= 6;
            case SECOND -> rowIndex <= 5;
        };

        if (!isValid) {
            displayManager.drawSnapshot(gs, "You cannot interact with your opponent's board!!");
        }

        return isValid;
    }

}

