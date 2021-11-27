package de.theminefighter.stslauncher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

public class IntegrationTests {

    private Process stsProcess;

    @Test
    @Timeout(4)
    @DisabledIfEnvironmentVariable(named = "CI",
            matches = "true", disabledReason = "Launching STS does not work in GUI-less CI systems.")
    public void runForUnknownHost() throws Exception {
        String filename = "sts-themi-invalidServer.jnlp";
        String altPath = Paths.get(".", "src", "test", "resources", filename).toString();
        String jnlp = new File(filename).exists()?filename: altPath;
        ProcessBuilder pb = new ProcessBuilder(Main.prepareLaunch(jnlp, false));
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
