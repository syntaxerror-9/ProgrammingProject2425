package it.unibz.inf.pp.clash.model;

import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

// Abstract away the move handling logic. This will make it easier to implement bots.
public interface MoveHandler {
    Snapshot handleMove(int rowIndex, int columnIndex, Snapshot currentSnapshot);
}
