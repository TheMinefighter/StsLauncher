package de.theminefighter.stslauncher.tests;
import de.theminefighter.stslauncher.LibManager;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
public class BasicLibTests {
    @Test
    public void checkLibUrls()  {
        String req="https://repo.maven.apache.org/maven2/com/sun/xml/ws/rt/2.3.1/rt-2.3.1.jar";
        assertTrue(Arrays.stream(LibManager.makeLibUrls()).map(URL::toString).anyMatch(req::equals));
    }
}
