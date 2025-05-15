package model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import it.unibz.inf.pp.clash.logic.Utils;
import it.unibz.inf.pp.clash.model.impl.TestEventHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.NormalizedBoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Butterfly;
import it.unibz.inf.pp.clash.view.DisplayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class EventHandlerTests {

    private DisplayManager displayManager;

    @BeforeEach
    public void setUp() {
        displayManager = Mockito.mock(DisplayManager.class);
        Input mockInput = Mockito.mock(Input.class);
        Gdx.input = mockInput;
    }

    @Test
    public void itShouldMoveAUnit() {
        var eventHandler = new TestEventHandler(displayManager);
        eventHandler.newGame("Hero1", "Hero2", (board, amount, unitConstructors) -> {
            var normalizedP1Board = NormalizedBoardImpl.createNormalizedBoard(board, Snapshot.Player.FIRST);
            normalizedP1Board.addUnit(0, new Butterfly(UnitColor.ONE));
        }, 3, 5);

        var snapshot = eventHandler.getSnapshot();
        Utils.PrintBoard(snapshot.getBoard());
        var normalizedPlayerBoard = NormalizedBoardImpl.createNormalizedBoard(snapshot.getBoard(), Snapshot.Player.FIRST);
        assertTrue(normalizedPlayerBoard.getUnit(0).isPresent());

        // Move the unit from (3, 0) to (3, 1)
        eventHandler.selectTile(3, 0);
        eventHandler.selectTile(3, 1);

        normalizedPlayerBoard = NormalizedBoardImpl.createNormalizedBoard(snapshot.getBoard(), Snapshot.Player.FIRST);
        assertTrue(normalizedPlayerBoard.getUnit(0).isEmpty());
        assertTrue(normalizedPlayerBoard.getUnit(1).isPresent());
    }

    @Test
    public void itShouldMoveAUnitToTheSameColumn() {
        var eventHandler = new TestEventHandler(displayManager);
        eventHandler.newGame("Hero1", "Hero2", (board, amount, unitConstructors) -> {
            var normalizedP1Board = NormalizedBoardImpl.createNormalizedBoard(board, Snapshot.Player.FIRST);
            normalizedP1Board.addUnit(0, new Butterfly(UnitColor.ONE));
        }, 3, 5);

        var snapshot = eventHandler.getSnapshot();
        var normalizedPlayerBoard = NormalizedBoardImpl.createNormalizedBoard(snapshot.getBoard(), Snapshot.Player.FIRST);
        assertTrue(normalizedPlayerBoard.getUnit(0).isPresent());

        Utils.PrintBoard(snapshot.getBoard());
        // Move the unit from (3, 0) to (3, 1)
        eventHandler.selectTile(3, 0);
        eventHandler.selectTile(3, 0);

        Utils.PrintBoard(snapshot.getBoard());
        normalizedPlayerBoard = NormalizedBoardImpl.createNormalizedBoard(snapshot.getBoard(), Snapshot.Player.FIRST);
        assertTrue(normalizedPlayerBoard.getUnit(0).isPresent());

        eventHandler.selectTile(3, 0);
        eventHandler.selectTile(4, 0);

        normalizedPlayerBoard = NormalizedBoardImpl.createNormalizedBoard(snapshot.getBoard(), Snapshot.Player.FIRST);
        assertTrue(normalizedPlayerBoard.getUnit(0).isPresent());

        eventHandler.selectTile(3, 0);
        eventHandler.selectTile(5, 0);

        normalizedPlayerBoard = NormalizedBoardImpl.createNormalizedBoard(snapshot.getBoard(), Snapshot.Player.FIRST);
        assertTrue(normalizedPlayerBoard.getUnit(0).isPresent());
    }
}
