package it.unibz.inf.pp.clash.model.impl;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.DummySnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.ONE;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.THREE;


public class TestEventHandler implements EventHandler {
    DisplayManager displayManager;
    DummyEventHandler dummyEventHandler;
    Snapshot snapshot;

    public TestEventHandler(DisplayManager displayManager) {
        this.dummyEventHandler = new DummyEventHandler(displayManager);
        this.displayManager = displayManager;
    }

    @Override
    public void newGame(String firstHero, String secondHero) {
        System.out.println("Starting a new game: " + firstHero + " vs " + secondHero);

        _unit = null;
        previousRowIndex = -1;
        previousColumnIndex = -1;
        currentMode = ActionMode.MOVE;

        snapshot = new GameSnapshot(
                new HeroImpl(firstHero, 20),
                new HeroImpl(secondHero, 20),
                BoardImpl.createEmptyBoard(11, 7),
                Snapshot.Player.FIRST,
                3
        );


        // units (putting them because otherwise it fucks up the board when creating it (no height of cells)
        snapshot.getBoard().addUnit(1, 4, new Butterfly(THREE));
        snapshot.getBoard().addUnit(6, 1, new Butterfly(THREE));
        snapshot.getBoard().addUnit(6, 2, new Butterfly(ONE));

        displayManager.drawSnapshot(snapshot, "New game started! " + firstHero + " vs " + secondHero);
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
        dummyEventHandler.callReinforcement();

    }

    @Override
    public void requestInformation(int rowIndex, int columnIndex) {
        dummyEventHandler.requestInformation(rowIndex, columnIndex);

    }

    Unit _unit = null;
    int previousRowIndex, previousColumnIndex;

// action mode and set mode method
    private ActionMode currentMode = ActionMode.MOVE;

    public void setMode(ActionMode mode) {
        this.currentMode = mode;
    }


    @Override
    public void selectTile(int rowIndex, int columnIndex) {
        switch (currentMode) {


            case MOVE -> handleMove(rowIndex, columnIndex);
            case DELETE -> handleDelete(rowIndex, columnIndex);

        }
    }

// move unit method (daniel thing)
    private void handleMove(int rowIndex, int columnIndex) {
        System.out.println("Hello");
        var unit = snapshot.getBoard().getUnit(rowIndex, columnIndex);
        if (unit.isPresent()) {
            System.out.println("Unit found");
            _unit = unit.get();
            previousRowIndex = rowIndex;
            previousColumnIndex = columnIndex;
            return;
        }

        if (_unit != null) {
            System.out.println("Placing here " + rowIndex + "  " + columnIndex);
            snapshot.getBoard().addUnit(rowIndex, columnIndex, _unit);
            snapshot.getBoard().removeUnit(previousRowIndex, previousColumnIndex);
            displayManager.drawSnapshot(snapshot, "Lule");
        }
    }


    // delete unit method
    private void handleDelete(int rowIndex, int columnIndex) {
        var unit = snapshot.getBoard().getUnit(rowIndex, columnIndex);

        if (unit.isPresent()) {

            System.out.println("Deleting the unit at " + rowIndex + " " + columnIndex);
            snapshot.getBoard().removeUnit(rowIndex, columnIndex);
            displayManager.drawSnapshot(snapshot, "Unit deleted! :D");
        } else {

            System.out.println("There is nothing to delete here uwu");
        }
    }





    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        dummyEventHandler.deleteUnit(rowIndex, columnIndex);

    }
}
