package it.unibz.inf.pp.clash.model.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import it.unibz.inf.pp.clash.model.BoardInitializer;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.MoveHandler;
import it.unibz.inf.pp.clash.model.movehandlers.HumanMoveHandler;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.NormalizedBoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;

import static it.unibz.inf.pp.clash.logic.GameSnapshotUtils.*;

import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

import java.util.Random;
import java.util.function.Function;


public class TestEventHandler implements EventHandler {
    DisplayManager displayManager;
    DummyEventHandler dummyEventHandler;
    Snapshot snapshot;
    MoveHandler moveHandler;

    public TestEventHandler(DisplayManager displayManager) {
        this.dummyEventHandler = new DummyEventHandler(displayManager);
        this.displayManager = displayManager;
        this.moveHandler = new HumanMoveHandler();
    }


    @Override
    public void newGame(String firstHero, String secondHero) {
        newGame(firstHero, secondHero, this::initializeBoardRandom, 7, 11);
    }

    public void newGame(String firstHero, String secondHero, BoardInitializer boardInitializer, int boardWidth, int boardHeight) {
        System.out.println("Starting a new game: " + firstHero + " vs " + secondHero);
        snapshot = new GameSnapshot(
                new HeroImpl(firstHero, 20),
                new HeroImpl(secondHero, 20),
                BoardImpl.createEmptyBoard(boardHeight, boardWidth),
                Snapshot.Player.FIRST,
                3
        );

        @SuppressWarnings("unchecked")
        Function<MobileUnit.UnitColor, MobileUnit>[] unitsConstructors = new Function[]{
                (Function<MobileUnit.UnitColor, MobileUnit>) Butterfly::new,
                (Function<MobileUnit.UnitColor, MobileUnit>) Unicorn::new
        };
        boardInitializer.apply(snapshot.getBoard(), 8, unitsConstructors);

        displayManager.drawSnapshot(snapshot, "New game started! " + firstHero + " vs " + secondHero);
    }


    private int findAvailableRandomSpot(NormalizedBoard normalizedBoard) {
        var random = new Random();
        int columnIndex = random.nextInt(normalizedBoard.getMaxColumnIndex() + 1);
        while (!normalizedBoard.canPlaceInColumn(columnIndex)) {
            columnIndex = random.nextInt(normalizedBoard.getMaxColumnIndex() + 1);
        }
        return columnIndex;
    }

    private void initializeBoardRandom(Board board, int amount, Function<MobileUnit.UnitColor, MobileUnit>[] unitConstructors) {
        var normalizedBoardP1 = NormalizedBoardImpl.createNormalizedBoard(board, Snapshot.Player.FIRST);
        var normalizedBoardP2 = NormalizedBoardImpl.createNormalizedBoard(board, Snapshot.Player.SECOND);

        var random = new Random();

        for (int i = 0; i < amount; i++) {
            int p1Index = findAvailableRandomSpot(normalizedBoardP1), p2Index = findAvailableRandomSpot(normalizedBoardP2);
            UnitColor p1Color = UnitColor.values()[random.nextInt(3)], p2Color = UnitColor.values()[random.nextInt(3)];
            MobileUnit unit1 = unitConstructors[random.nextInt(unitConstructors.length)].apply(p1Color), unit2 = unitConstructors[random.nextInt(unitConstructors.length)].apply(p2Color);
            normalizedBoardP1.addUnit(p1Index, unit1);
            normalizedBoardP2.addUnit(p2Index, unit2);

        }
    }


    @Override
    public void exitGame() {
        System.out.println("Exiting the game.");


        snapshot = null;
        _unit = null;
        previousRowIndex = -1;
        previousColumnIndex = -1;


        displayManager.drawHomeScreen();
    }

    @Override
    public void skipTurn() {
        if (snapshot == null) {
            System.out.println("Teh game is not active.");
            return;
        }

        if (!(snapshot instanceof GameSnapshot gs)) {
            System.out.println("Cannot skip the turn, the snapshot is wrong.");
            return;
        }

        gs.switchTurn();

        System.out.println("Turn skipped. The active player is now: " + gs.getActivePlayer());
        System.out.println("Remaining actions: " + gs.getNumberOfRemainingActions());

        displayManager.drawSnapshot(gs, "Turn skipped. It's now " + gs.getActivePlayer() + "'s turn.");
    }


    @Override
    public void callReinforcement() {
        if (!(snapshot instanceof GameSnapshot)) return;
        GameSnapshot gs = (GameSnapshot) snapshot;

        Snapshot.Player player = gs.getActivePlayer();
        int reinforcements = gs.getSizeOfReinforcement(player);

        if (reinforcements <= 0) {
            displayManager.drawSnapshot(gs, "No reinforcements available.");
            return;
        }

        int[] targetRows = (player == Snapshot.Player.FIRST) ? new int[]{6, 7, 8} : new int[]{3, 4, 5};
        placeRandomButterflies(gs, reinforcements, targetRows);

        consumeAction(gs, displayManager);
    }


    @Override
    public void requestInformation(int rowIndex, int columnIndex) {
        dummyEventHandler.requestInformation(rowIndex, columnIndex);

    }

    Unit _unit = null;
    int previousRowIndex, previousColumnIndex;


    @Override
    public void selectTile(int rowIndex, int columnIndex) {

        // action mode (move or delete)
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            handleDelete(rowIndex, columnIndex);
        } else {
            System.out.println("Handle move");
            handleMove(rowIndex, columnIndex);
        }


    }

    // UPDATED move unit method (now you can overlay 2 same color units for an upgrade)
    private void handleMove(int rowIndex, int columnIndex) {
        if (!isTileOwnedByActivePlayer(snapshot, rowIndex, displayManager)) return;

        moveHandler.handleMove(rowIndex, columnIndex, snapshot);

        displayManager.drawSnapshot(snapshot, "Moved.");
    }


    // delete unit method
    private void handleDelete(int rowIndex, int columnIndex) {
        var unit = snapshot.getBoard().getUnit(rowIndex, columnIndex);

        if (!isTileOwnedByActivePlayer(snapshot, rowIndex, displayManager)) return;

        if (unit.isPresent()) {

            System.out.println("Deleting the unit at " + rowIndex + " " + columnIndex);
            snapshot.getBoard().removeUnit(rowIndex, columnIndex);
            displayManager.drawSnapshot(snapshot, "Unit deleted! :D");
            consumeAction((GameSnapshot) snapshot, displayManager);
        } else {

            System.out.println("There is nothing to delete here uwu");
        }
    }


    //non serve
    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        dummyEventHandler.deleteUnit(rowIndex, columnIndex);
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }


}
