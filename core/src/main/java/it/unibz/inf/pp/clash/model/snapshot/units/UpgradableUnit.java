package it.unibz.inf.pp.clash.model.snapshot.units;

public interface UpgradableUnit {
    int getLevel();

    void upgrade(int amount);
}
