package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.Optional;
import java.util.Stack;

public class NormalizedBoardImpl implements NormalizedBoard {


    private Board board;
    private Snapshot.Player player;
    private Stack<Unit>[] normalizedBoard;

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
        return (board.getMaxRowIndex() + 1) / 2;
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
    public boolean canPlaceInColumn(int columnIndex) {
        var stack = normalizedBoard[columnIndex];
        return stack.size() < getMaxRowIndex();
    }

    @Override
    public void addUnit(int rowIndex, int columnIndex, Unit unit) {
        checkBoundaries(rowIndex, columnIndex);
        board.addUnit(getRealRowIndex(rowIndex), columnIndex, unit);
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
    }


    @Override
    public void removeUnit(int columnIndex) {
        var stack = normalizedBoard[columnIndex];
        var stackTop = stack.size() - 1;
        if (stackTop >= 0) {
            stack.pop();
            removeUnit(stackTop, columnIndex);
        }
    }

    @Override
    public int getNormalizedRowIndex(int rowIndex) {
        if (player == Snapshot.Player.FIRST) {
            // map 6->0 7->1 .. 11->5
            return rowIndex - ((board.getMaxRowIndex() + 1) / 2);
        } else {
            // map 5->0 4->1 .. 0->5
            return Math.abs(rowIndex - (board.getMaxRowIndex()) / 2);
        }
    }

    private int getRealRowIndex(int normalizedRowIndex) {
        if (player == Snapshot.Player.FIRST) {
            return normalizedRowIndex + ((board.getMaxRowIndex() + 1) / 2);
        } else {
            return Math.abs(normalizedRowIndex - (board.getMaxRowIndex()) / 2);
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
}
