package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.formation.Formation;
import it.unibz.inf.pp.clash.model.impl.GameEventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.*;


public class NormalizedBoardImpl implements NormalizedBoard {


    private Board board;
    private Snapshot.Player player;
    // TODO: A dequeue might be more appropriate
    private Stack<Unit>[] normalizedBoard;
    final private Set<Formation> formations = new HashSet<>();

    public static NormalizedBoard createNormalizedBoard(Board board, Snapshot.Player player) {
        var normalizedBoard = new NormalizedBoardImpl();
        normalizedBoard.board = board;
        normalizedBoard.player = player;
        normalizedBoard.initializeNormalizedBoard();
        return normalizedBoard;
    }

    @Override
    public int getMaxColumnIndex() {
        return board.getMaxColumnIndex();
    }

    @Override
    public int getMaxRowIndex() {
        return (board.getMaxRowIndex()) / 2;
    }

    @Override
    public boolean areValidCoordinates(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex <= getMaxRowIndex() && columnIndex >= 0 && columnIndex <= getMaxColumnIndex();
    }

    @Override
    public Optional<Unit> getUnit(int rowIndex, int columnIndex) {
        checkBoundaries(rowIndex, columnIndex);
        if (rowIndex >= normalizedBoard[columnIndex].size() || rowIndex < 0) return Optional.empty();
        return Optional.ofNullable(normalizedBoard[columnIndex].get(rowIndex));
    }

    @Override
    public Optional<Unit> getUnit(int columnIndex) {
        return getUnit(Math.max(normalizedBoard[columnIndex].size() - 1, 0), columnIndex);
    }

    @Override
    public Snapshot.Player getPlayer() {
        return player;
    }

    @Override
    public boolean canPlaceInColumn(int columnIndex) {
        var stack = normalizedBoard[columnIndex];
        return stack.size() <= getMaxRowIndex();
    }

    @Override
    public boolean isFull() {
        return Arrays.stream(normalizedBoard).allMatch(stack -> stack.size() >= (getMaxRowIndex() + 1));
    }

    @Override
    public void addUnit(int rowIndex, int columnIndex, Unit unit) {
        checkBoundaries(rowIndex, columnIndex);
        applyFormations(checkForFormations());
        applyBoardState();
    }

    @Override
    public void addUnit(int columnIndex, Unit unit) {
        var stack = normalizedBoard[columnIndex];
        var stackTop = stack.size() - 1;
        stack.push(unit);
        addUnit(stackTop + 1, columnIndex, unit);
    }

    @Override
    public void removeUnit(int rowIndex, int columnIndex) {
        checkBoundaries(rowIndex, columnIndex);
        board.removeUnit(getRealRowIndex(rowIndex), columnIndex);
        var stack = normalizedBoard[columnIndex];
        stack.remove(rowIndex);
        applyFormations(checkForFormations());
        applyBoardState();

    }


    @Override
    public void removeUnit(int columnIndex) {
        var stack = normalizedBoard[columnIndex];
        var stackTop = stack.size() - 1;
        if (stackTop >= 0) {
            removeUnit(stackTop, columnIndex);
        }
    }

    @Override
    public int normalizeRowIndex(int rowIndex) {
        var middle = (board.getMaxRowIndex() + 1) / 2;
        if (player == Snapshot.Player.FIRST) {
            // map 6->0 7->1 .. 11->5
            return rowIndex - middle;
        } else {
            // map 5->0 4->1 .. 0->5
            return middle - rowIndex - 1;
        }
    }

    private int getRealRowIndex(int normalizedRowIndex) {
        var middle = (board.getMaxRowIndex() + 1) / 2;
        if (player == Snapshot.Player.FIRST) {
            return normalizedRowIndex + middle;
        } else {
            return Math.abs(normalizedRowIndex - middle + 1);
        }
    }

    @Override
    public Stack<Unit>[] getNormalizedBoard() {
        return normalizedBoard;
    }

    private void initializeNormalizedBoard() {
        var stacks = new Stack[getMaxColumnIndex() + 1];

        for (int i = 0; i < stacks.length; i++) {
            var stack = new Stack<Unit>();
            stacks[i] = stack;

            for (int j = 0; j < getMaxRowIndex(); j++) {
                var unit = board.getUnit(getRealRowIndex(j), i);
                unit.ifPresent(stack::push);
            }
        }
        normalizedBoard = stacks;
    }


    private void checkBoundaries(int rowIndex, int columnIndex) {
        if (!areValidCoordinates(rowIndex, columnIndex)) {
            throw new CoordinatesOutOfBoardException(rowIndex, columnIndex, getMaxRowIndex(), getMaxColumnIndex());
        }
    }

    private void applyFormations(List<Formation> newFormations) {
        if (newFormations.isEmpty()) return;
        for (var newFormation : newFormations) {
            var formationsInColumn = formations.stream().filter(f -> f.columnIndex() == newFormation.columnIndex());
            int maxRowIndex = formationsInColumn.map(formation -> formation.rowIndex() + 3).max(Integer::compareTo).orElse(0);
            var stack = normalizedBoard[newFormation.columnIndex()];
            stack.removeAll(newFormation.units());
            stack.addAll(maxRowIndex, newFormation.units());
            formations.add(new Formation(maxRowIndex, newFormation.columnIndex(), newFormation.units(), newFormation.isAttackingFormation()));
        }
    }

    public int takeDamage(int damage, int column) {
        var stack = normalizedBoard[column];
        if (stack.isEmpty()) return damage;
        var unit = stack.firstElement();
        while (damage > 0 && !stack.isEmpty()) {
            if (unit.getHealth() <= damage) {
                damage -= unit.getHealth();
                // TODO: Check if the unit was in a formation
                stack.remove(unit);
            } else {
                unit.setHealth(unit.getHealth() - damage);
                damage = 0;
            }
            if (!stack.isEmpty()) {
                unit = stack.firstElement();
            }
        }
        applyBoardState();
        return damage;

    }

    public void updateFormations(Hero enemyHero, NormalizedBoard enemyBoard) {
        var formationsToRemove = new ArrayList<Formation>();
        for (var formation : formations) {
            formation.update();

            if (formation.shouldBeDestroyed()) {
                formationsToRemove.add(formation);
                if (formation.isAttackingFormation()) {
                    var formationDamage = formation.getFormationAttackDamage();
                    var heroDamage = enemyBoard.takeDamage(formationDamage, formation.columnIndex());
                    enemyHero.setHealth(enemyHero.getHealth() - heroDamage);
                    applyBoardState();
                }
                normalizedBoard[formation.columnIndex()].removeAll(formation.units());
            }
        }
        if (formationsToRemove.isEmpty()) return;

        formations.removeAll(formationsToRemove);
        applyBoardState();
    }

    // Checks the board for conditions and appends them to the formations list
    private List<Formation> checkForFormations() {

        var newFormations = new ArrayList<Formation>();
        for (int i = 0; i < normalizedBoard.length; i++) {
            var stack = normalizedBoard[i];
            // Check for attacking formations
            if (stack.size() >= 3) {
                var formationCount = 0;
                Unit previousElement = null;
                for (int j = 0; j < stack.size(); j++) {

                    int finalI = i;
                    int finalJ = j;
                    if (formations.stream().anyMatch(f -> f.columnIndex() == finalI && f.rowIndex() <= finalJ && finalJ < f.rowIndex() + 3)) {
                        // This formation is already present. Skip it.
                        continue;
                    }
                    // If previous element is not null
                    if (previousElement != null) {
                        // Then check if the previous element is a mobile unit
                        if (previousElement instanceof MobileUnit previousMobileUnit && stack.get(j) instanceof MobileUnit currentMobileUnit) {
                            // If it is a mobile unit, then check if it matches
                            if (previousMobileUnit.matches(currentMobileUnit)) {
                                // If it matches, then we should add it to the formation count
                                formationCount++;
                            } else {
                                // If it doesn't match, then we restart the formation count
                                formationCount = 1;
                                previousElement = stack.get(j);
                            }
                        } else {
                            // If it's not a mobile unit, then it's a wall and it doesn't count.
                            previousElement = null;
                            formationCount = 0;
                        }
                    } else {
                        // If it's null, we either just started or we have a wall
                        previousElement = stack.get(j);
                        if (previousElement instanceof MobileUnit) {
                            formationCount = 1;
                        } else {
                            formationCount = 0;
                        }
                    }

                    if (formationCount == 3) {
                        // We have an attack formation vertically
                        var formationStart = j - 2;
                        var formation = new ArrayList<>(stack.subList(formationStart, j + 1));
                        newFormations.add(new Formation(j, i, formation, true));
                    }
                }
            }
        }

        return newFormations;
    }

    // Applies the normalized board state to the actual board. Making sure they're always in sync.
    void applyBoardState() {
        // Completely reset this player's side of the board
        for (int i = 0; i <= getMaxColumnIndex(); i++) {
            for (int j = 0; j <= getMaxRowIndex(); j++) {
                var row = getRealRowIndex(j);
                var col = i;
                board.getUnit(row, col).ifPresent(u -> board.removeUnit(row, col));
            }
        }

        for (int i = 0; i <= getMaxColumnIndex(); i++) {
            var stack = normalizedBoard[i];
            for (int j = 0; j < stack.size(); j++) {
                var row = getRealRowIndex(j);
                var col = i;
                board.addUnit(row, col, stack.get(j));
            }
        }

    }

}
