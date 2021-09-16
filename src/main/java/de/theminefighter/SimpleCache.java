package de.theminefighter;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SimpleCache implements ResourceCache {
    private static final Path cacheRoot = Paths.get(System.getProperty("user.home"), "STSLauncher");

    public SimpleCache() throws IOException {
        if (!Files.exists(cacheRoot)) Files.createDirectory(cacheRoot.toAbsolutePath());
    }

    public URL get(URL remoteResource, boolean noUpdate) throws IOException {
        File f = getFileByUrl(remoteResource);
        ensureCached(remoteResource, f, noUpdate);
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private File getFileByUrl(URL remoteResource) {
        return new File(cacheRoot.toString(), toOsCompliantFileName(remoteResource));

    }

    private String toOsCompliantFileName(URL remoteResource) {
        return remoteResource.toString().replace("/", "-..-")
                .replace("[", "(").replaceAll("]", "(")
                .replace(":", "-...-");
    }

    private void ensureCached(URL remoteResource, File localCache, boolean noUpdate) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) remoteResource.openConnection();
        if (localCache.exists()) {
            if (noUpdate) return;
            connection.setIfModifiedSince(localCache.lastModified());
        }
        connection.connect();
        switch (connection.getResponseCode()) {
            case 304:
                return;
            case 200:
                java.nio.file.Files.copy(connection.getInputStream(), localCache.toPath(), REPLACE_EXISTING);
            default:
                throw new IOException("Error fetching " + remoteResource);
        }

    }
}
