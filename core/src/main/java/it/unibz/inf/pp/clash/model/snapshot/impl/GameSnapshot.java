package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

public class GameSnapshot extends AbstractSnapshot {

    public GameSnapshot(Hero firstHero, Hero secondHero, Board board, Player activePlayer, int actionsRemaining) {
        super(firstHero, secondHero, board, activePlayer, actionsRemaining, null);
    }

    public void switchTurn() {
        var previousPlayer = this.activeplayer;
        this.activeplayer = (this.activeplayer == Player.FIRST) ? Player.SECOND : Player.FIRST;
        this.actionsRemaining = 3;
        this.ongoingMove = null;

        var activePlayerBoard = getNormalizedBoard(activeplayer);
        activePlayerBoard.updateFormations(getHero(previousPlayer), getNormalizedBoard(previousPlayer));

    }


    public void decrementActions() {
        if (this.actionsRemaining > 0) {
            this.actionsRemaining--;
        }
    }


    public void setActionsRemaining(int count) {
        this.actionsRemaining = count;
    }

    public void clearOngoingMove() {
        this.ongoingMove = null;
    }

    @Override
    public int getSizeOfReinforcement(Player player) {
        return 3;
    }

}

