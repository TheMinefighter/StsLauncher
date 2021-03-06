package de.theminefighter.stslauncher;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
@DisabledIfEnvironmentVariable(named = "CI",
        matches = "true", disabledReason = "Launching STS does not work in GUI-less CI systems.")
public class IntegrationTests {

    private Process stsProcess;
    private String[] args;

    @BeforeEach
    public void buildCache() throws Exception {
        String filename = "sts-themi-invalidServer.jnlp";
        String altPath = Paths.get(".", "src", "test", "resources", filename).toString();
        String jnlp = new File(filename).exists()?filename: altPath;
        args = Main.prepareLaunch(jnlp, false);
    }
    @Test
    @Timeout(5)
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
    @AfterEach
    public void cleanup() {
        if (stsProcess!=null)
            stsProcess.destroyForcibly();

    }
}
