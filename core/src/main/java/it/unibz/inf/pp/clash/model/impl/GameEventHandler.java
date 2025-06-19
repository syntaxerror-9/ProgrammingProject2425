package it.unibz.inf.pp.clash.model.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import it.unibz.inf.pp.clash.logic.GameSnapshotUtils;
import it.unibz.inf.pp.clash.model.BoardInitializer;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.MoveHandler;
import it.unibz.inf.pp.clash.model.movehandlers.HumanMoveHandler;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;

import it.unibz.inf.pp.clash.model.snapshot.impl.NormalizedBoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.UnitUtils;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.BoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;

import static it.unibz.inf.pp.clash.logic.GameSnapshotUtils.*;

import it.unibz.inf.pp.clash.model.snapshot.units.impl.Unicorn;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

import java.util.Random;
import java.util.function.Function;


public class GameEventHandler implements EventHandler {

    DisplayManager displayManager;
    Snapshot snapshot;
    MoveHandler moveHandler;

    static private GameEventHandler instance;

    // TODO: This probably should only listen to the events and delegate the logic to another class
    public GameEventHandler(DisplayManager displayManager) {
        if (instance != null) throw new RuntimeException("GameEventHandler should be a singleton");
        instance = this;
        this.displayManager = displayManager;
        moveHandler = new HumanMoveHandler();
    }


    public static GameEventHandler getInstance() {
        return instance;
    }


    @Override
    public void newGame(String firstHero, String secondHero) {
        newGame(firstHero, secondHero, this::initializeBoardRandom, 7, 11);
        var firstTurnHero = snapshot.getHero(snapshot.getActivePlayer());
        if (firstTurnHero.isBot()) {
            GameSnapshotUtils.doBotTurn((GameSnapshot) snapshot, displayManager);
        }
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

        boardInitializer.apply(snapshot.getBoard(), 8, UnitUtils.mobileUnitsConstructors());
        displayManager.drawSnapshot(snapshot, "New game started! " + firstHero + " vs " + secondHero);
    }


    private int findAvailableRandomSpot(NormalizedBoard normalizedBoard) {
        if (normalizedBoard.isFull()) {
            System.out.println("The board is full, no available spots.");
            return -1;
        }


        var random = new Random();
        int columnIndex = random.nextInt(normalizedBoard.getMaxColumnIndex() + 1);


        while (!normalizedBoard.canPlaceInColumn(columnIndex)) {
            columnIndex = random.nextInt(normalizedBoard.getMaxColumnIndex() + 1);
        }
        return columnIndex;
    }

    private void initializeBoardRandom(Board board, int amount, Function<MobileUnit.UnitColor, MobileUnit>[] unitConstructors) {
        var random = new Random();
        for (int i = 0; i < amount; i++) {
            int p1Index = findAvailableRandomSpot(snapshot.getNormalizedBoard(Player.FIRST)), p2Index = findAvailableRandomSpot(snapshot.getNormalizedBoard(Player.SECOND));
            UnitColor p1Color = UnitColor.values()[random.nextInt(3)], p2Color = UnitColor.values()[random.nextInt(3)];
            MobileUnit unit1 = unitConstructors[random.nextInt(unitConstructors.length)].apply(p1Color), unit2 = unitConstructors[random.nextInt(unitConstructors.length)].apply(p2Color);
            snapshot.getNormalizedBoard(Player.FIRST).addUnit(p1Index, unit1);
            snapshot.getNormalizedBoard(Player.SECOND).addUnit(p2Index, unit2);

        }
    }


    @Override
    public void exitGame() {
        snapshot = null;
        displayManager.drawHomeScreen();
    }

    @Override
    public void skipTurn() {
        handleSkipTurn(false);
    }

    public void skipTurn(boolean isBotTurn) {
        handleSkipTurn(isBotTurn);
    }

    private void handleSkipTurn(boolean isBotTurn) {

        if (snapshot == null) {
            System.out.println("Teh game is not active.");
            return;
        }

        if (!(snapshot instanceof GameSnapshot gs)) {
            System.out.println("Cannot skip the turn, the snapshot is wrong.");
            return;
        }
        var currentHero = gs.getHero(gs.getActivePlayer());
        if (!isBotTurn && currentHero.isBot()) {
            System.out.println("Cannot skip turn for enemy bot.");
            return;

        }


        Snapshot.Player previousPlayer = gs.getActivePlayer();
        GameSnapshotUtils.switchTurn((GameSnapshot) snapshot, displayManager);
        // TODO: When base project is completed
//        handleTurnEndAbilities(gs, previousPlayer);

        System.out.println("Turn skipped. The active player is now: " + gs.getActivePlayer());
        System.out.println("Remaining actions: " + gs.getNumberOfRemainingActions());

        displayManager.drawSnapshot(gs, "Turn skipped. It's now " + gs.getActivePlayer() + "'s turn.");
    }


    @Override
    public void callReinforcement() {
        if (!(snapshot instanceof GameSnapshot)) return;
        if (moveHandler instanceof HumanMoveHandler) {
            ((HumanMoveHandler) moveHandler).resetPreviousColumnIndex();
        }
        GameSnapshot gs = (GameSnapshot) snapshot;
        var currentBoard = snapshot.getCurrentBoard();

        Snapshot.Player player = gs.getActivePlayer();
        int reinforcements = gs.getSizeOfReinforcement(player);

        if (reinforcements <= 0) {
            displayManager.drawSnapshot(gs, "No reinforcements available.");
            return;
        }

        for (int i = 0; i < reinforcements; i++) {
            int columnIndex = findAvailableRandomSpot(currentBoard);

            UnitColor color = UnitColor.values()[new Random().nextInt(3)];
            var mobileUnitsConstructors = UnitUtils.mobileUnitsConstructors();
            currentBoard.addUnit(columnIndex, mobileUnitsConstructors[new Random().nextInt(mobileUnitsConstructors.length)].apply(color));
        }

        consumeAction(gs, displayManager);
    }


    @Override
    public void requestInformation(int rowIndex, int columnIndex) {
        // TODO: Arshad implement this method
    }

    @Override
    public void selectTile(int rowIndex, int columnIndex) {
        handleMove(rowIndex, columnIndex, false);
    }

    // This function will get called by the bot player.
    public void selectTile(int rowIndex, int columnIndex, boolean isBotMove) {
        handleMove(rowIndex, columnIndex, isBotMove);
    }


    // UPDATED move unit method (now you can overlay 2 same color units for an upgrade)
    private void handleMove(int rowIndex, int columnIndex, boolean isBotMove) {
        if (!isTileOwnedByActivePlayer(snapshot, rowIndex, displayManager, isBotMove)) return;

        if (moveHandler.handleMove(rowIndex, columnIndex, snapshot.getCurrentBoard())) {
            displayManager.drawSnapshot(snapshot, "Moved.");
            consumeAction((GameSnapshot) snapshot, displayManager);
        } else {
            displayManager.drawSnapshot(snapshot, "Not moved");
        }

    }


    // delete unit method
    // TODO: Check if unit is a attack formation. If it is, it cannot be destroyed.
    // Walls can be deleted.
    private void handleDelete(int rowIndex, int columnIndex, boolean isBotMove) {
        if (!isTileOwnedByActivePlayer(snapshot, rowIndex, displayManager, isBotMove)) return;
        if (moveHandler instanceof HumanMoveHandler) {
            ((HumanMoveHandler) moveHandler).resetPreviousColumnIndex();
        }

        var board = snapshot.getCurrentBoard();
        var unit = board.getUnit(board.normalizeRowIndex(rowIndex), columnIndex);

        if (unit.isPresent()) {
            board.removeUnit(board.normalizeRowIndex(rowIndex), columnIndex);
            displayManager.drawSnapshot(snapshot, "Unit deleted! :D");
            consumeAction((GameSnapshot) snapshot, displayManager);
        } else {

            System.out.println("There is nothing toColumn delete here uwu");
        }
    }


    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        handleDelete(rowIndex, columnIndex, false);
    }

    public void deleteUnit(int rowIndex, int columnIndex, boolean isBotMove) {
        handleDelete(rowIndex, columnIndex, isBotMove);
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }


}
