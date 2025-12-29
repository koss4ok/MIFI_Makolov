import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> result = new ArrayList<>();

        int rows = unitsByRow.size();
        if (rows == 0) return result;

        List<Set<Integer>> aliveColumnsByRow = new ArrayList<>(rows);

        for (int row = 0; row < rows; row++) {
            Set<Integer> aliveColumns = new HashSet<>();
            List<Unit> currentRow = unitsByRow.get(row);

            if (currentRow != null) {
                for (Unit u : currentRow) {
                    if (u != null && u.isAlive()) {
                        aliveColumns.add(u.getyCoordinate());
                    }
                }
            }
            aliveColumnsByRow.add(aliveColumns);
        }

        for (int row = 0; row < rows; row++) {
            List<Unit> currentRow = unitsByRow.get(row);
            if (currentRow == null || currentRow.isEmpty()) continue;

            int neighborRow = isLeftArmyTarget ? row + 1 : row - 1;
            Set<Integer> neighborAliveColumns =
                    (neighborRow >= 0 && neighborRow < rows)
                            ? aliveColumnsByRow.get(neighborRow)
                            : Collections.emptySet();

            for (Unit unit : currentRow) {
                if (unit == null || !unit.isAlive()) continue;

                if (!neighborAliveColumns.contains(unit.getyCoordinate())) {
                    result.add(unit);
                }
            }
        }

        return result;
    }
}
