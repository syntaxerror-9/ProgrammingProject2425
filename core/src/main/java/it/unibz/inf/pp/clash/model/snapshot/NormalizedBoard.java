package it.unibz.inf.pp.clash.model.snapshot;

import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.Optional;
import java.util.Stack;

/**
 * A normalized board is a board that has the same coordinates for all players.
 * It is an array of stacks, where each stack represents a column.
 */
public interface NormalizedBoard extends Board {
    int normalizeRowIndex(int rowIndex);

    Stack<Unit>[] getNormalizedBoard();

    void removeUnit(int columnIndex);

    void addUnit(int columnIndex, Unit unit);

    Optional<Unit> getUnit(int columnIndex);

    Snapshot.Player getPlayer();

    boolean canPlaceInColumn(int columnIndex);

    void updateFormations(Hero enemyHero, NormalizedBoard enemyBoard);

    boolean isFull();

    int takeDamage(int damage, int column);

}
