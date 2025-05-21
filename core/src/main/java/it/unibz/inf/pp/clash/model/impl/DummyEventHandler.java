package it.unibz.inf.pp.clash.model.impl;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.AnotherDummySnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.DummySnapshot;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

/**
 * This is a dummy implementation, for demonstration purposes.
 * It should not appear in the final project.
 */
public class DummyEventHandler implements EventHandler {

    private final DisplayManager displayManager;

    public DummyEventHandler(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

    @Override
    public void selectTile(int rowIndex, int columnIndex) {
        try {
            displayManager.updateMessage(
                    String.format(
                            "Tile (%s,%s) has just been selected",
                            rowIndex,
                            columnIndex
                    ));
        } catch (NoGameOnScreenException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requestInformation(int rowIndex, int columnIndex) {
        try {
            displayManager.updateMessage(
                    String.format(
                            "Information request for Tile (%s,%s).",
                            rowIndex,
                            columnIndex
                    ));
        } catch (NoGameOnScreenException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        try {
            displayManager.updateMessage(
                    String.format(
                            "Unit deletion request for Tile (%s,%s)",
                            rowIndex,
                            columnIndex
                    ));
        } catch (NoGameOnScreenException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void newGame(String firstHero, String secondHero) {
        displayManager.drawSnapshot(
                new DummySnapshot(
                        firstHero,
                        secondHero
                ), "This is a dummy game snapshot, for demonstration purposes."
        );
    }

   @Override
public void callReinforcement() {
    System.out.println("Calling reinforcements. Reinforcement count: " + reinforcementCount);
    // simulate consuming one reinforcement unit
    if (reinforcementCount > 0) {
        reinforcementCount--;
        displayManager.drawSnapshot(
            new AnotherDummySnapshot("Alice", "Bob"),
            "Reinforcement deployed! Remaining: " + reinforcementCount
        );
    } else {
        try {
            displayManager.updateMessage("No reinforcements available!");
        } catch (NoGameOnScreenException e) {
            throw new RuntimeException(e);
        }
    }
}


    @Override
    public void skipTurn() {
        displayManager.drawSnapshot(
                new DummySnapshot(
                        "Alice",
                        "Bob"
                ), "This is a dummy game snapshot, for demonstration purposes."
        );

    }

    @Override
    public void exitGame() {
        displayManager.drawHomeScreen();
    }
    private int reinforcementCount = 0;

@Override
public void onUnitDeath(String unitType) {
    reinforcementCount++;
    System.out.println("Unit of type " + unitType + " died. Reinforcement count: " + reinforcementCount);
}

}
