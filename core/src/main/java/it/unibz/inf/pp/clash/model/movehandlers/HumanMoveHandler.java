package it.unibz.inf.pp.clash.model.movehandlers;

import it.unibz.inf.pp.clash.model.MoveHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.NormalizedBoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.UpgradableUnit;

public class HumanMoveHandler implements MoveHandler {

    int previousColumnIndex = -1;

    @Override
    public boolean handleMove(int rowIndex, int columnIndex, NormalizedBoard board) {


        if (previousColumnIndex == -1) {
            if (board.getUnit(board.normalizeRowIndex(rowIndex), columnIndex).isPresent()) {
                previousColumnIndex = columnIndex;
            }
        } else {
            var selectedUnit = board.getUnit(previousColumnIndex);
            rowIndex = board.normalizeRowIndex(rowIndex);

            if (selectedUnit.isPresent() && selectedUnit.get() instanceof MobileUnit selectedMobileUnit) {

                if (isMoveUnitMove(board, selectedMobileUnit, columnIndex, rowIndex)) {
                    moveTurn(board, selectedMobileUnit, columnIndex);
                } else if (isUpgradeUnitMove(board, selectedMobileUnit, columnIndex, rowIndex)) {
                    upgradeTurn(board, selectedMobileUnit, columnIndex, rowIndex);
                } else {
                    System.out.printf("Invalid move from column %d to column %d", previousColumnIndex, columnIndex);
                    previousColumnIndex = -1;
                    return false;
                }
                System.out.printf("Moved unit from column %d to column %d", previousColumnIndex, columnIndex);
                previousColumnIndex = -1;

                return true;
            }

        }
        return false;
    }

    private void moveTurn(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex) {
        playerBoard.removeUnit(previousColumnIndex);
        playerBoard.addUnit(columnIndex, selectedUnit);
    }

    private void upgradeTurn(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex, int rowIndex) {
        playerBoard.removeUnit(previousColumnIndex);
        var targetUnit = playerBoard.getUnit(rowIndex, columnIndex);
        if (targetUnit.isPresent() && targetUnit.get() instanceof UpgradableUnit upgradableUnit) {
            upgradableUnit.upgrade(selectedUnit.getLevel());
        }
    }

    private boolean isMoveUnitMove(NormalizedBoard playerBoard, MobileUnit selectedUnit, int columnIndex, int rowIndex) {
        if (isUpgradeUnitMove(playerBoard, selectedUnit, columnIndex, rowIndex)) return false;
        if (columnIndex == previousColumnIndex) return false;
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
