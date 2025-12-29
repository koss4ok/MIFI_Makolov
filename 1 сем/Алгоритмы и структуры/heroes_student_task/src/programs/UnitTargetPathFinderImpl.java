import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int W = 27;
    private static final int H = 21;

    @Override
    public List<Edge> getTargetPath(Unit attackUnit,
                                    Unit targetUnit,
                                    List<Unit> existingUnitList) {

        if (targetUnit == null || !targetUnit.isAlive()) {
            return Collections.emptyList();
        }

        boolean[][] blocked = new boolean[W][H];
        for (Unit u : existingUnitList) {
            if (u != attackUnit && u != targetUnit) {
                blocked[u.getxCoordinate()][u.getyCoordinate()] = true;
            }
        }

        int sx = attackUnit.getxCoordinate();
        int sy = attackUnit.getyCoordinate();
        int tx = targetUnit.getxCoordinate();
        int ty = targetUnit.getyCoordinate();

        int[][] dist = new int[W][H];
        for (int i = 0; i < W; i++) {
            Arrays.fill(dist[i], -1);
        }

        Edge[][] parent = new Edge[W][H];
        ArrayDeque<Edge> q = new ArrayDeque<>();

        dist[sx][sy] = 0;
        q.add(new Edge(sx, sy));

        int[] dx = {-1,-1,-1, 0,0, 1,1,1};
        int[] dy = {-1, 0, 1,-1,1,-1,0,1};

        while (!q.isEmpty()) {
            Edge cur = q.poll();

            if (cur.getX() == tx && cur.getY() == ty) {
                break;
            }

            for (int k = 0; k < 8; k++) {
                int nx = cur.getX() + dx[k];
                int ny = cur.getY() + dy[k];

                if (nx < 0 || ny < 0 || nx >= W || ny >= H) continue;
                if (blocked[nx][ny]) continue;
                if (dist[nx][ny] != -1) continue;

                dist[nx][ny] = dist[cur.getX()][cur.getY()] + 1;
                parent[nx][ny] = cur;
                q.add(new Edge(nx, ny));
            }
        }

        if (dist[tx][ty] == -1) {
            return Collections.emptyList();
        }

        List<Edge> path = new ArrayList<>();
        Edge cur = new Edge(tx, ty);

        while (cur != null) {
            path.add(cur);
            cur = parent[cur.getX()][cur.getY()];
        }

        Collections.reverse(path);
        return path;
    }
}
