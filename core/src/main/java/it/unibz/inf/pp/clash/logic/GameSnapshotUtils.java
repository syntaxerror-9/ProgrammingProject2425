package it.unibz.inf.pp.clash.logic;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;

import java.util.Random;

import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import it.unibz.inf.pp.clash.model.snapshot.Snapshot;


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
            Snapshot.Player previousPlayer = gs.getActivePlayer();
            gs.switchTurn();
            // TODO: When base project is completed
//            handleTurnEndAbilities(gs, previousPlayer);

            displayManager.drawSnapshot(gs, "Turn ended. Now it's " + gs.getActivePlayer() + "'s turn.");
        } else {
            displayManager.drawSnapshot(gs, "Action performed. Remaining: " + remaining);
        }
    }


    //checking who can interact with what (no cheating!!!)
    public static boolean isTileOwnedByActivePlayer(Snapshot snapshot, int rowIndex, DisplayManager displayManager) {
        if (!(snapshot instanceof GameSnapshot gs)) return false;

        boolean isValid = switch (gs.getActivePlayer()) {
            case FIRST -> rowIndex >= (snapshot.getBoard().getMaxRowIndex() / 2) + 1;
            case SECOND -> rowIndex <= (snapshot.getBoard().getMaxRowIndex() / 2);
        };

        if (!isValid) {
            displayManager.drawSnapshot(gs, "You cannot interact with your opponent's board!!");
        }

        return isValid;
    }

    //triggers the abilities at the end of turns
    public static void handleTurnEndAbilities(GameSnapshot gs, Snapshot.Player playerEndingTurn) {

        triggerFairyAbilities(gs, playerEndingTurn);
        //put other abilities here
    }


    //fairy ability
    public static void triggerFairyAbilities(GameSnapshot gs, Snapshot.Player previousPlayer) {
        Board board = gs.getBoard();
        int maxRow = board.getMaxRowIndex();
        int maxCol = board.getMaxColumnIndex();
        Random random = new Random();
        UnitColor[] colors = UnitColor.values();

        for (int row = 0; row <= maxRow; row++) {
            for (int col = 0; col <= maxCol; col++) {
                var unit = board.getUnit(row, col);
                if (unit.isPresent() && unit.get() instanceof Fairy) {

                    // where is the fairy owo
                    boolean isFairyOwnedByPlayer = switch (previousPlayer) {
                        case FIRST -> row >= 6;
                        case SECOND -> row <= 5;
                    };

                    if (!isFairyOwnedByPlayer) continue;

                    List<Board.TileCoordinates> freeAdjacent = new ArrayList<>();

                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int nr = row + dr;
                            int nc = col + dc;

                            if ((dr != 0 || dc != 0) &&
                                    board.areValidCoordinates(nr, nc) &&
                                    board.getUnit(nr, nc).isEmpty()) {

                                boolean isFriendlyZone = switch (previousPlayer) {
                                    case FIRST -> nr >= 6;
                                    case SECOND -> nr <= 5;
                                };

                                if (isFriendlyZone) {
                                    freeAdjacent.add(new Board.TileCoordinates(nr, nc));
                                }
                            }
                        }
                    }

                    if (!freeAdjacent.isEmpty()) {
                        var chosen = freeAdjacent.get(random.nextInt(freeAdjacent.size()));
                        UnitColor randomColor = colors[random.nextInt(colors.length)];
                        board.addUnit(chosen.rowIndex(), chosen.columnIndex(), new Butterfly(randomColor));
                    }

                }

            }
        }
    }


}

