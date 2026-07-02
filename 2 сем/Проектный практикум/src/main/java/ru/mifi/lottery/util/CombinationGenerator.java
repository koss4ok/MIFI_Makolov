package ru.mifi.lottery.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class CombinationGenerator {

    private static final SecureRandom rnd = new SecureRandom();

    public static int[] generateUniqueNumbers(int count, int minInclusive, int maxInclusive) {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        if (minInclusive > maxInclusive) throw new IllegalArgumentException("min must be <= max");

        Set<Integer> set = new HashSet<>(count * 2);
        int target = count;
        while (set.size() < target) {
            int n = minInclusive + rnd.nextInt(maxInclusive - minInclusive + 1);
            set.add(n);
        }

        int[] res = new int[target];
        int i = 0;
        for (int v : set) res[i++] = v;
        java.util.Arrays.sort(res);
        return res;
    }
}
