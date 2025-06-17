package it.unibz.inf.pp.clash.model.movehandlers;

import it.unibz.inf.pp.clash.model.MoveHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;

public class RandomRobotMoveHandler implements MoveHandler {
    @Override
    public boolean handleMove(int rowIndex, int columnIndex, NormalizedBoard board) {
        return false;
    }
}
