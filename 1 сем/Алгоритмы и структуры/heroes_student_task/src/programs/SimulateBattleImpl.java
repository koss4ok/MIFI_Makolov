import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulateBattleImpl implements SimulateBattle {

    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {

        List<Unit> queue = new ArrayList<>();
        for (Unit u : playerArmy.getUnits()) {
            if (u.isAlive()) queue.add(u);
        }
        for (Unit u : computerArmy.getUnits()) {
            if (u.isAlive()) queue.add(u);
        }

        if (queue.isEmpty()) return;



        while (true) {
            queue.sort(Comparator.comparingInt(Unit::getBaseAttack).reversed());
            boolean someoneAttacked = false;

            for (Unit unit : queue) {
                if (!unit.isAlive()) continue;

                Unit target = unit.getProgram().attack();

                if (target != null && target.isAlive()) {
                    printBattleLog.printBattleLog(unit, target);
                    someoneAttacked = true;
                }
            }

            // 4. Проверка завершения боя
            if (!someoneAttacked ||
                    isArmyDead(playerArmy) ||
                    isArmyDead(computerArmy)) {
                return;
            }
        }
    }

    private boolean isArmyDead(Army army) {
        for (Unit u : army.getUnits()) {
            if (u.isAlive()) return false;
        }
        return true;
    }
}
