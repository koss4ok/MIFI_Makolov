import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {

    private static final int MAX_PER_TYPE = 11;
    private static final int FIELD_HEIGHT = 21;

    private static final int START_X = 0;
    private static final int END_X = 2;

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        Army army = new Army();
        army.setUnits(new ArrayList<>());
        army.setPoints(0);

        if (unitList == null || unitList.isEmpty() || maxPoints <= 0) {
            return army;
        }

        List<Unit> sortedTypes = new ArrayList<>(unitList);
        sortedTypes.sort(
                Comparator.comparingDouble(this::efficiency).reversed()
        );

        Map<String, Integer> counters = new HashMap<>();
        for (Unit u : sortedTypes) {
            counters.put(u.getUnitType(), 0);
        }

        int x = START_X;
        int y = 0;

        boolean added;
        do {
            added = false;

            for (Unit base : sortedTypes) {
                int count = counters.get(base.getUnitType());
                if (count >= MAX_PER_TYPE) continue;
                if (army.getPoints() + base.getCost() > maxPoints) continue;
                if (y >= FIELD_HEIGHT) return army;

                Unit unit = new Unit(
                        base.getName() + " " + (count + 1),
                        base.getUnitType(),
                        base.getHealth(),
                        base.getBaseAttack(),
                        base.getCost(),
                        base.getAttackType(),
                        base.getAttackBonuses(),
                        base.getDefenceBonuses(),
                        x,
                        y
                );
                unit.setProgram(base.getProgram());

                army.getUnits().add(unit);
                army.setPoints(army.getPoints() + unit.getCost());
                counters.put(base.getUnitType(), count + 1);

                x++;
                if (x > END_X) {
                    x = START_X;
                    y++;
                }

                added = true;
            }
        } while (added);

        return army;
    }

    private double efficiency(Unit u) {
        return (double) u.getBaseAttack() / u.getCost()
                + 0.01 * ((double) u.getHealth() / u.getCost());
    }
}
