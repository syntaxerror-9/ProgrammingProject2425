package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

import java.util.function.Function;

public class UnitUtils {
    public static Function<MobileUnit.UnitColor, MobileUnit>[] mobileUnitsConstructors() {
        return new Function[]{(Function<MobileUnit.UnitColor, MobileUnit>) Butterfly::new,
                (Function<MobileUnit.UnitColor, MobileUnit>) Unicorn::new,
                (Function<MobileUnit.UnitColor, MobileUnit>) Fairy::new};
    }
}
