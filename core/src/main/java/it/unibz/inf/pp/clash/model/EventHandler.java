package it.unibz.inf.pp.clash.model;

import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

public interface EventHandler {

    /**
     * This method is called when the user starts a new game.
     *
     * @param firstHero name of the hero for the first player
     * @param secondHero name of the hero for the second player
     */
    void newGame(String firstHero, String secondHero);

    /**
     * This method is called if there is an ongoing game and the user interrupts it.
     */
    void exitGame();

    /**
     * This method is called if the active player decides to skip his/her turn.
     */
    void skipTurn();

    /**
     * This method is called when the user calls reinforcements.
     * <p>
     * Note that this may be an "invalid" action.
     * For instance (depending on the rules of the game):
     * - if there is no reinforcement unit,
     * - if there is an ongoing move,
     * - etc.
     */
    void callReinforcement();

    /**
     * This method is called if the user asks for information about the tile ({@code rowIndex}, {@code columnIndex}),
     * or the unit standing on it.
     */
    void requestInformation(int rowIndex, int columnIndex);

    /**
     * This method is called if the user selects the tile with coordinates ({@code rowIndex}, {@code columnIndex}).
     * This tile may for instance be the arrival tile of the ongoing move (if any), or the initial tile of a new move.
     * <p>
     * Note that the coordinates may be "incorrect".
     * For instance (depending on the rules of the game):
     * - if the tile is not on the board of the active player (but instead on the board of the other player),
     * - if there is no ongoing move and the tile is empty,
     * - etc.
     */
    void selectTile(int rowIndex, int columnIndex);

    /**
     * This method is called if the user tries to delete the unit standing on the tile with coordinates
     * ({@code rowIndex}, {@code columnIndex}).
     * <p>
     * Note that the coordinates may be "incorrect".
     * For instance (depending on the rules of the game):
     * - if there is no unit on this tile,
     * - if the unit on this tile cannot be deleted,
     * - if there is an ongoing move,
     * - etc.
     */
    void deleteUnit(int rowIndex, int columnIndex);
   void onUnitDeath(String unitType);

}
