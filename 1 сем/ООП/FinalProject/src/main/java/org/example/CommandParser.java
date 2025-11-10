package org.example;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    public static String[] split(String line) {
        if (line == null)
            return new String[0];
        line = line.trim();
        if (line.isEmpty())
            return new String[0];
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                quoted = !quoted;
            }
            else if (Character.isWhitespace(ch) && !quoted) {
                if (!cur.isEmpty()) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
            }
            else {
                cur.append(ch);
            }
        }
        if (!cur.isEmpty())
            out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    public static String joinFrom(String[] arr, int idx) {
        StringBuilder sb = new StringBuilder();
        for (int i = idx; i < arr.length; i++) {
            if (i > idx)
                sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
