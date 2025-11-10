package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    @Test
    void splitHandlesSpacesAndQuotes() {
        String[] parts = CommandParser.split("expense 99.5 FOOD \"late dinner\"");
        assertArrayEquals(new String[]{"expense","99.5","FOOD","late dinner"}, parts);
    }

    @Test
    void splitEmptyLineGivesZero() {
        assertEquals(0, CommandParser.split("   ").length);
    }

    @Test
    void joinFromWorks() {
        String[] arr = {"income","100","GENERAL","salary","bonus"};
        assertEquals("salary bonus", CommandParser.joinFrom(arr,3));
    }
}
