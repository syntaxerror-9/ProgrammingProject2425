package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

import java.util.Optional;

import static it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;

public abstract class AbstractSnapshot implements Snapshot {

    protected final Board board;
    protected final NormalizedBoard normalizedBoardP1, normalizedBoardP2;
    private final Hero firstHero;
    private final Hero secondHero;

    protected Player activeplayer;
    protected int actionsRemaining;
    protected TileCoordinates ongoingMove;


    protected AbstractSnapshot(Hero firstHero, Hero secondHero, Board board, Player activeplayer, int actionsRemaining,
                               TileCoordinates ongoingMove) {
        this.board = board;
        this.firstHero = firstHero;
        this.secondHero = secondHero;
        this.activeplayer = activeplayer;
        this.actionsRemaining = actionsRemaining;
        normalizedBoardP1 = NormalizedBoardImpl.createNormalizedBoard(board, Player.FIRST);
        normalizedBoardP2 = NormalizedBoardImpl.createNormalizedBoard(board, Player.SECOND);
        this.ongoingMove = ongoingMove;
    }

    @Override
    public NormalizedBoard getNormalizedBoard(Player player) {
        return switch (player) {
            case FIRST -> normalizedBoardP1;
            case SECOND -> normalizedBoardP2;
        };
    }

    @Override
    public NormalizedBoard getCurrentBoard() {
        return switch (activeplayer) {
            case FIRST -> normalizedBoardP1;
            case SECOND -> normalizedBoardP2;
        };
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public Hero getHero(Player player) {
        return switch (player) {
            case FIRST -> firstHero;
            case SECOND -> secondHero;
        };
    }

    @Override
    public Player getActivePlayer() {
        return activeplayer;
    }

    @Override
    public Optional<TileCoordinates> getOngoingMove() {
        return Optional.ofNullable(ongoingMove);
    }

    @Override
    public int getNumberOfRemainingActions() {
        return actionsRemaining;
    }

}
