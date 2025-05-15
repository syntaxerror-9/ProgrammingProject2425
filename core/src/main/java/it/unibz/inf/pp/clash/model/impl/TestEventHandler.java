package it.unibz.inf.pp.clash.model.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import static it.unibz.inf.pp.clash.logic.GameSnapshotUtils.*;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import static it.unibz.inf.pp.clash.logic.GameSnapshotUtils.handleTurnEndAbilities;


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

        snapshot = new GameSnapshot(
                new HeroImpl(firstHero, 20),
                new HeroImpl(secondHero, 20),
                BoardImpl.createEmptyBoard(11, 7),
                Snapshot.Player.FIRST,
                3
        );


        // units (putting them because otherwise it fucks up the board when creating it (no height of cells)
        placeRandomButterflies((GameSnapshot) snapshot, 5, new int[]{6, 7, 8});
        placeRandomButterflies((GameSnapshot) snapshot, 5, new int[]{3, 4, 5});

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

        Snapshot.Player previousPlayer = gs.getActivePlayer();
        gs.switchTurn();
        handleTurnEndAbilities(gs, previousPlayer);


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
            handleMove(rowIndex, columnIndex);
        }



    }

// UPDATED move unit method (now you can overlay 2 same color units for an upgrade)
private void handleMove(int rowIndex, int columnIndex) {
    if (!isTileOwnedByActivePlayer(snapshot, rowIndex, displayManager)) return;

    var clicked = snapshot.getBoard().getUnit(rowIndex, columnIndex);

    if (clicked.isPresent() && _unit == null) {
        _unit = clicked.get();
        previousRowIndex = rowIndex;
        previousColumnIndex = columnIndex;
        displayManager.drawSnapshot(snapshot, "Unit selected.");
        return;
    }

    if (_unit == null) return;

    // second click
    if (clicked.isPresent()) {

        //checkups
        if (!(_unit instanceof MobileUnit moving)) {
            displayManager.drawSnapshot(snapshot, "Only mobile units can move.");
            return;
        }

        if (rowIndex == previousRowIndex && columnIndex == previousColumnIndex) {
            displayManager.drawSnapshot(snapshot, "Nice try hehe.");
            return;
        }

        var target = clicked.get();

        if (!(target instanceof MobileUnit targetMobile)) {
            displayManager.drawSnapshot(snapshot, "Cannot move there.");
            return;
        }

        if (!moving.getColor().equals(targetMobile.getColor())) {
            displayManager.drawSnapshot(snapshot, "Cannot move onto a unit of different color.");
            return;
        }

        if (!moving.getClass().equals(targetMobile.getClass())) {
            displayManager.drawSnapshot(snapshot, "You cant fuse two different classes.");
            return;
        }

        // merging units
        if (moving.getClass().equals(Unicorn.class)) {
            snapshot.getBoard().removeUnit(rowIndex, columnIndex);
            snapshot.getBoard().removeUnit(previousRowIndex, previousColumnIndex);
            snapshot.getBoard().addUnit(rowIndex, columnIndex, new Fairy(moving.getColor()));

            _unit = null;
            previousRowIndex = -1;
            previousColumnIndex = -1;

            displayManager.drawSnapshot(snapshot, "Final Upgrade! A wild fairy appears.");
            consumeAction((GameSnapshot) snapshot, displayManager);
            return;
        }

        snapshot.getBoard().removeUnit(rowIndex, columnIndex);
        snapshot.getBoard().removeUnit(previousRowIndex, previousColumnIndex);
        snapshot.getBoard().addUnit(rowIndex, columnIndex, new Unicorn(moving.getColor()));

        _unit = null;
        previousRowIndex = -1;
        previousColumnIndex = -1;

        displayManager.drawSnapshot(snapshot, "Upgrade!!! An unicorn appears.");
        consumeAction((GameSnapshot) snapshot, displayManager);
        return;
    }

    // normal movement
    snapshot.getBoard().addUnit(rowIndex, columnIndex, _unit);
    snapshot.getBoard().removeUnit(previousRowIndex, previousColumnIndex);

    _unit = null;
    previousRowIndex = -1;
    previousColumnIndex = -1;

    displayManager.drawSnapshot(snapshot, "Moved.");
    consumeAction((GameSnapshot) snapshot, displayManager);
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




}
