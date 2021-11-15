package de.theminefighter.StsLauncher;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SimpleCache implements ResourceCache {
    private static final Path cacheRoot = Paths.get(System.getProperty("user.home"), "STSLauncher");

    public SimpleCache() throws IOException {
        if (!Files.exists(cacheRoot)) Files.createDirectory(cacheRoot.toAbsolutePath());
    }

    public URL get(URL remoteResource, boolean noUpdate){
        File f = getFileByUrl(remoteResource);
        try {
            ensureCached(remoteResource, f, noUpdate);
            return f.toURI().toURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFileByUrl(URL remoteResource) {
        return new File(cacheRoot.toString(), toOsCompliantFileName(remoteResource));

    }
    public static String toSHA224(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-224");
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new BigInteger(1,md.digest(data.getBytes(StandardCharsets.UTF_8))).toString(32);
    }

    private String toOsCompliantFileName(URL remoteResource) {
        return toSHA224(remoteResource.toString());
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
                break;
            default:
                throw new IOException("Error fetching " + remoteResource);
        }

    }
}
