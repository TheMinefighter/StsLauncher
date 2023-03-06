package de.theminefighter.stslauncher;

import de.theminefighter.stslauncher.caching.ResourceCache;
import de.theminefighter.stslauncher.caching.SimpleCache;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CacheTests {
    final ResourceCache r = new SimpleCache();

    public CacheTests() throws IOException {
    }

    @Test
    @Order(0)
    public void cache() throws Exception {
        String req = "https://github.com/TheMinefighter/StsLauncher/releases/download/v1.2/StsLauncher.jar";
        File cacheUrl = r.get(new URL(req), false);
        assertEquals(15409, cacheUrl.length());
    }
    @Test
    @Order(10)
    public void License() throws Exception {
        String req = "https://github.com/TheMinefighter/StsLauncher/releases/download/v1.2/StsLauncher.jar";
        File cacheUrl = r.get(new URL(req), true);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            LibManager.showLicenseFile(ps,cacheUrl);
        }
        String data = baos.toString(utf8);
        assertTrue(data.contains("Permission is hereby granted, free of charge, to any person obtaining a copy"));
    }

	@Test
	public void basics() throws MalformedURLException {
		assertFalse(r.has(new URL("riugherjkhglierhngkjsrb.de")));
	}
}
