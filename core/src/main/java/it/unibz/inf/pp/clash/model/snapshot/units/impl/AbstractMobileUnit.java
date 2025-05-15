package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public abstract class AbstractMobileUnit extends AbstractUnit implements MobileUnit {

    final UnitColor color;
    int attackCountDown = -1;
    int level = 1;

    protected AbstractMobileUnit(int health, UnitColor color) {
        super(health);
        this.color = color;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void upgrade(int amount) {
        if (level < 2) {
            level = Math.min(level + amount, 2);
        }
    }

    @Override
    public UnitColor getColor() {
        return color;
    }

    @Override
    public int getAttackCountdown() {
        return attackCountDown;
    }

    @Override
    public void setAttackCountdown(int attackCountDown) {
        this.attackCountDown = attackCountDown;
    }
}
