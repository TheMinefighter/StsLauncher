package de.theminefighter.stslauncher;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OtherTests {
    @Test
    public void JavaPathTest() {
        File file = new File(Main.getJavaPath());
        assertTrue(file.exists());
        assertTrue(file.canExecute());
    }
}
