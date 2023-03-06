package de.theminefighter.stslauncher;

import org.junit.jupiter.api.Test;

import javax.jnlp.impl.IntegrationServiceLinuxImpl;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OtherTests {
    @Test
    public void JavaPathTest() {
        File file = new File(JavaUtilities.getJavaPath());
        assertTrue(file.exists());
        assertTrue(file.canExecute());
    }

	@Test
	public void InitialShortCutTest() {
		System.setProperty("jnlpx.origFilenameArg", IntegrationTests.getJnlpPath());
		Main.createRequestedShortcuts();
		IntegrationServiceLinuxImpl isl = new IntegrationServiceLinuxImpl();
		assertTrue(isl.hasDesktopShortcut());
		assertTrue(isl.hasMenuShortcut());
	}
}
