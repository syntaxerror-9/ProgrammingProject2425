package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.formation.Formation;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wall;

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
        checkAndApplyDefensiveFormation(player);
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
        checkAndApplyDefensiveFormation(player);
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

            // here: attacking formations will not replace walls anymore
            int insertIndex = 0;
            // finding the closest wall
            insertIndex = 0;
            for (int i = 0; i < stack.size(); i++) {
                Unit unit = stack.get(i);
                if (unit instanceof Wall) {
                    insertIndex = i + 1;
                } else if (unit instanceof MobileUnit) {

                    break;

                } else {
                    insertIndex = i;
                }
            }

            // moving units to make space (not the walls)
            List<Unit> toShift = new ArrayList<>();
            for (int i = insertIndex; i < stack.size(); i++) {
                Unit u = stack.get(i);
                if (u instanceof MobileUnit) {
                    toShift.add(u);
                } else {

                    break;

                }
            }

            // removing the units (only the mobile ones)
            for (Unit u : toShift) {
                stack.remove(u);
            }

            // putting the formation
            for (int i = 0; i < newFormation.units().size(); i++) {
                int idx = insertIndex + i;
                if (idx < stack.size()) {
                    stack.set(idx, newFormation.units().get(i));
                } else {
                    stack.add(newFormation.units().get(i));
                }
            }

            // only adding if not on a wall
            for (int i = 0; i < toShift.size(); i++) {
                int index = insertIndex + newFormation.units().size() + i;

                if (index >= stack.size()) {
                    stack.add(toShift.get(i));
                } else {
                    Unit u = stack.get(index);

                    if (u == null || u instanceof MobileUnit) {
                        stack.set(index, toShift.get(i));
                    } else {

                        System.out.println("Not moving walls. " + newFormation.columnIndex());
                    }
                }
            }

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
        formations.removeAll(formationsToRemove);

        var newAttack = checkForFormations();
        applyFormations(newAttack);

        applyBoardState();
    }

    // lore: I commented your checkFormations method, had to make another otherwhise it doesn't recognise formations when the first element of the column is a wall.
//    //  Checks the board for conditions and appends them to the formations list
//    private List<Formation> checkForFormations() {
//
//        var newFormations = new ArrayList<Formation>();
//        for (int i = 0; i < normalizedBoard.length; i++) {
//            var stack = normalizedBoard[i];
//            // Check for attacking formations
//            if (stack.size() >= 3) {
//                var formationCount = 0;
//                Unit previousElement = null;
//                for (int j = 0; j < stack.size(); j++) {
//
//                    int finalI = i;
//                    int finalJ = j;
//                    if (formations.stream().anyMatch(f -> f.columnIndex() == finalI && f.rowIndex() <= finalJ && finalJ < f.rowIndex() + 3)) {
//                        // This formation is already present. Skip it.
//                        continue;
//                    }
//                    // If previous element is not null
//                    if (previousElement != null) {
//                        // Then check if the previous element is a mobile unit
//                        if (previousElement instanceof MobileUnit previousMobileUnit && stack.get(j) instanceof MobileUnit currentMobileUnit) {
//                            // If it is a mobile unit, then check if it matches
//                            if (previousMobileUnit.matches(currentMobileUnit)) {
//                                // If it matches, then we should add it to the formation count
//                                formationCount++;
//                            } else {
//                                // If it doesn't match, then we restart the formation count
//                                formationCount = 1;
//                                previousElement = stack.get(j);
//                            }
//                        } else {
//                            // If it's not a mobile unit, then it's a wall and it doesn't count.
//                            previousElement = null;
//                            formationCount = 0;
//                        }
//                    } else {
//                        // If it's null, we either just started or we have a wall
//                        previousElement = stack.get(j);
//                        if (previousElement instanceof MobileUnit) {
//                            formationCount = 1;
//                        } else {
//                            formationCount = 0;
//                        }
//                    }
//
//                    if (formationCount == 3) {
//                        // We have an attack formation vertically
//                        var formationStart = j - 2;
//                        var formation = new ArrayList<>(stack.subList(formationStart, j + 1));
//                        newFormations.add(new Formation(j, i, formation, true));
//                    }
//                }
//            }
//        }
//
//        return newFormations;
//    }

    // new check formation with a sliding window logic: now it recognises formations when there are walls in the first row.
    // TODO: fix bug -> formations get bigger than 3 units in some cases
    private List<Formation> checkForFormations() {

        var newFormations = new ArrayList<Formation>();
        for (int i = 0; i < normalizedBoard.length; i++) {
            var stack = normalizedBoard[i];

            // checking for attacking formations
            if (stack.size() >= 3) {
                // sliding window, finding 3 consecutive mobile units
                for (int j = 0; j <= stack.size() - 3; j++) {

                    int finalI = i;
                    int finalJ = j;

                    // this formation is already present. Skip it.
                    if (formations.stream().anyMatch(f -> f.columnIndex() == finalI && f.rowIndex() <= finalJ && finalJ < f.rowIndex() + 3)) {
                        continue;
                    }

                    Unit u1 = stack.get(j);
                    Unit u2 = stack.get(j + 1);
                    Unit u3 = stack.get(j + 2);

                    if (u1 instanceof MobileUnit m1 && u2 instanceof MobileUnit m2 && u3 instanceof MobileUnit m3) {
                        if (m1.matches(m2) && m1.matches(m3)) {
                            // we have an attack formation vertically
                            List<Unit> formation = new ArrayList<>(Arrays.asList(m1, m2, m3));
                            newFormations.add(new Formation(j, i, formation, true));

                        }
                    }
                }
            }
        }

        return newFormations;
    }


    public void checkAndApplyDefensiveFormation(Snapshot.Player currentPlayer) {
        int maxRow = getMaxRowIndex();
        int maxCol = getMaxColumnIndex();

        for (int row = 0; row <= maxRow; row++) {
            MobileUnit previousMobile = null;
            int count = 0;
            List<Unit> chain = new ArrayList<>();
            int startCol = 0;

            for (int col = 0; col <= maxCol; col++) {
                var unitOpt = getUnit(row, col);
                if (unitOpt.isPresent() && unitOpt.get() instanceof MobileUnit currentMobile) {
                    if (previousMobile != null && previousMobile.matches(currentMobile)) {
                        chain.add(currentMobile);
                        count++;
                    } else {
                        if (count >= 3) {
                            applyWallChainCentered(chain, startCol, currentPlayer);
                        }
                        previousMobile = currentMobile;
                        chain.clear();
                        chain.add(currentMobile);
                        count = 1;
                        startCol = col;
                    }
                } else {
                    if (count >= 3) {
                        applyWallChainCentered(chain, startCol, currentPlayer);
                    }
                    previousMobile = null;
                    chain.clear();
                    count = 0;
                }
            }

            if (count >= 3) {
                applyWallChainCentered(chain, startCol, currentPlayer);
            }
        }
    }


    private void applyWallChainCentered(List<Unit> units, int startCol, Snapshot.Player currentPlayer) {
        int length = units.size();
        int centerCol = startCol + (length / 2);
        int firstCol = centerCol - (length / 2);

        // removing units
        for (var unit : units) {
            for (var stack : normalizedBoard) {
                stack.remove(unit);
            }
        }

        int preferredRow = (currentPlayer == Snapshot.Player.FIRST) ? 6 : 5;
        int normalizedPreferred = normalizeRowIndex(preferredRow);

        for (int i = 0; i < length; i++) {
            int col = firstCol + i;
            if (col < 0 || col >= normalizedBoard.length) continue;

            Stack<Unit> stack = normalizedBoard[col];

            // first row without a wall
            int targetIndex = normalizedPreferred;
            while (targetIndex < getMaxRowIndex() + 1 && targetIndex < stack.size() && stack.get(targetIndex) instanceof Wall) {
                targetIndex++;
            }

            while (stack.size() <= targetIndex) {
                stack.add(null);
            }

            // shift if necessary
            boolean hasShiftableUnits = false;
            for (int j = targetIndex; j < stack.size(); j++) {
                Unit u = stack.get(j);
                if (u instanceof MobileUnit) {
                    hasShiftableUnits = true;
                    break;
                }
            }

            if (hasShiftableUnits) {
                for (int j = stack.size() - 1; j >= targetIndex; j--) {
                    if (j + 1 >= stack.size()) {
                        stack.add(stack.get(j));
                    } else {
                        stack.set(j + 1, stack.get(j));
                    }
                }
            }

            stack.set(targetIndex, new Wall());
        }

        System.out.println("Placed a new wall chain.");
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
