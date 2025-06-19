package it.unibz.inf.pp.clash.model.bot;

import it.unibz.inf.pp.clash.model.impl.GameEventHandler;
import it.unibz.inf.pp.clash.model.snapshot.NormalizedBoard;
import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.EmptyStackException;

public interface BotPlayer {

    // How much the bot has to wait to play the nextMove
    public static int BOT_MOVE_DELAY = 1000;

    static BotPlayer getBotPlayerFromName(String name) {
        return new ProceduralBot();
    }

    void PlayMove(GameSnapshot gs);

}
