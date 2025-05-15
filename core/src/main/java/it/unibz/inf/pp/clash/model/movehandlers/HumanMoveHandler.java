package it.unibz.inf.pp.clash.model.movehandlers;

import it.unibz.inf.pp.clash.model.MoveHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.NormalizedBoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.UpgradableUnit;

public class HumanMoveHandler implements MoveHandler {


    int previousColumnIndex = -1;

    @Override
    public Snapshot handleMove(int rowIndex, int columnIndex, Snapshot currentSnapshot) {
        var board = currentSnapshot.getBoard();

        var playerBoard = NormalizedBoardImpl.createNormalizedBoard(board, currentSnapshot.getActivePlayer());

        if (previousColumnIndex == -1) {
            previousColumnIndex = columnIndex;
            System.out.println("Column index " + columnIndex + " row index " + rowIndex);
        } else {
            var selectedUnit = playerBoard.getUnit(previousColumnIndex);
            rowIndex = playerBoard.getNormalizedRowIndex(rowIndex);

            if (selectedUnit.isPresent() && selectedUnit.get() instanceof MobileUnit selectedMobileUnit) {

                if (isMoveUnitMove(playerBoard, selectedMobileUnit, columnIndex, rowIndex)) {
                    handleMove(playerBoard, selectedMobileUnit, columnIndex);
                } else if (isUpgradeUnitMove(playerBoard, selectedMobileUnit, columnIndex, rowIndex)) {
                    handleUpgrade(playerBoard, selectedMobileUnit, columnIndex, rowIndex);
                } else {
                    System.out.println("Invalid move");
                }
                previousColumnIndex = -1;
            }

        }
        return currentSnapshot;
    }

    private void handleMove(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex) {
        playerBoard.removeUnit(previousColumnIndex);
        playerBoard.addUnit(columnIndex, selectedUnit);
    }

    private void handleUpgrade(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex, int rowIndex) {
        playerBoard.removeUnit(previousColumnIndex);
        var targetUnit = playerBoard.getUnit(rowIndex, columnIndex);
        if (targetUnit.isPresent() && targetUnit.get() instanceof UpgradableUnit upgradableUnit) {
            upgradableUnit.upgrade(selectedUnit.getLevel());
        }
    }

    private boolean isMoveUnitMove(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex, int rowIndex) {
        if (isUpgradeUnitMove(playerBoard, selectedUnit, columnIndex, rowIndex)) return false;
        return playerBoard.canPlaceInColumn(columnIndex);
    }

    private boolean isUpgradeUnitMove(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex, int rowIndex) {
        var targetUnitMaybe = playerBoard.getUnit(rowIndex, columnIndex);
        if (targetUnitMaybe.isPresent() && targetUnitMaybe.get() instanceof MobileUnit targetUnit) {
            if (selectedUnit == targetUnit) return false;
            // The unit is a mobile unit. Now we need to check they match the type and color.
            return targetUnit.getClass() == selectedUnit.getClass() && targetUnit.getColor() == selectedUnit.getColor();
        }
        return false;

    }
}
