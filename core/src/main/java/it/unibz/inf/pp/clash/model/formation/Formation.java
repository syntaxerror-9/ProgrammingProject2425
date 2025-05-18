package it.unibz.inf.pp.clash.model.formation;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;

import java.util.List;

public record Formation(int rowIndex, int columnIndex, List<Unit> units, boolean isAttackingFormation) {


    public boolean shouldBeDestroyed() {
        return units.stream().allMatch(unit -> {
            if (unit instanceof MobileUnit mobileUnit) return mobileUnit.getAttackCountdown() <= 0;
            // TODO CHECK FOR WALLS
            return false;
        });
    }

    public Formation(int rowIndex, int columnIndex, List<Unit> units, boolean isAttackingFormation) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.units = units;
        this.isAttackingFormation = isAttackingFormation;

        for (Unit unit : units) {
            if (unit instanceof MobileUnit mobileUnit) {
                mobileUnit.setAttackCountdown(mobileUnit.getInitialAttackCountdown());
            }
        }
    }

    // Returns true if the formation should be destroyed.
    public void update() {
        if (units.isEmpty()) {
            System.err.println("Formations should not be empty");
            return;
        }

        for (Unit unit : units) {
            if (unit instanceof MobileUnit mobileUnit) {
                mobileUnit.setAttackCountdown(mobileUnit.getAttackCountdown() - 1);
            }
        }
    }

    public int getFormationAttackDamage() {
        if (!isAttackingFormation) return 0;
        return units.stream()
                .filter(unit -> unit instanceof MobileUnit)
                .mapToInt(unit -> ((MobileUnit) unit).getAttackDamage())
                .sum();
    }

    public int getFormationHealth() {
        if (!isAttackingFormation) return -1;
        return units.stream()
                .mapToInt(Unit::getHealth)
                .sum();
    }

}
