package it.unibz.inf.pp.clash.model;

import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

public interface MoveHandler {

    /**
     * @return boolean indicating if the move was an action or not.
     **/
    boolean handleMove(int rowIndex, int columnIndex, NormalizedBoard board);
}
