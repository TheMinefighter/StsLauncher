package de.theminefighter.stslauncher;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.jnlp.impl.IntegrationServiceLinuxImpl;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfEnvironmentVariable(named = "CI",
        matches = "true", disabledReason = "Launching STS does not work in GUI-less CI systems.")
public class IntegrationTests {

    private Process stsProcess;
    private String[] args;

    @BeforeEach
    public void buildCache() throws Exception {
		String jnlp = getJnlpPath();
		args = JnlpLauncher.prepareLaunch(jnlp, false);
    }

	public static String getJnlpPath() {
		String filename = "sts-themi-invalidServer.jnlp";
		String altPath = Paths.get(".", "src", "test", "resources", filename).toString();
		return new File(filename).exists()?filename: altPath;
	}

	@Test
    @Timeout(10)
    @DisabledIfEnvironmentVariable(named = "CI",
            matches = "true", disabledReason = "Launching STS does not work in GUI-less CI systems.")
    public void runForUnknownHost() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        stsProcess = pb.start();

        InputStream errorStream = stsProcess.getInputStream();
        StringBuilder res= new StringBuilder();
        while (true) {
            if (errorStream.available()!=0) {
                res.append((char) errorStream.read());
            if (res.toString().contains("java.net.UnknownHostException: www.stellwerksim.de.nonExistingDomainTest")) return;
            }

        }
    }
	@Test
	@DisabledIfEnvironmentVariable(named = "CI",
			matches = "true", disabledReason = "Desktop and menu folder are not available in CI")
	public void InitialShortCutTest() {
		System.setProperty("jnlpx.origFilenameArg", IntegrationTests.getJnlpPath());
		Main.createRequestedShortcuts();
		IntegrationServiceLinuxImpl isl = new IntegrationServiceLinuxImpl();
		assertTrue(isl.hasDesktopShortcut());
		assertTrue(isl.hasMenuShortcut());
	}
    @AfterEach
    public void cleanup() {
        if (stsProcess!=null)
            stsProcess.destroyForcibly();

    }
}
