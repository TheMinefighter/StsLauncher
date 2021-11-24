package de.theminefighter.stsLauncher;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SimpleCache implements ResourceCache {
    /**
     * The root dir of this cache
     */
    private static final Path cacheRoot = Paths.get(System.getProperty("user.home"), "STSLauncher");

    /**
     * Initializes a new cache
     * @throws IOException when the cache dir cannot be found or created
     */
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

    /**
     * Builds the path under which a given remote resource might be cached
     * @param remoteResource the resource to cache
     * @return a File object, describing were to cache the given remote resource
     */
    private File getFileByUrl(URL remoteResource) {
        return new File(cacheRoot.toString(), toOsCompliantFileName(remoteResource));

    }

    /**
     * Creates a (close to) unique String from a given URL, which is compliant with the file name rules of most operating systems
     * @apiNote  I use a Hash, because for long urls any two way encoding (like B64) would be to long and therefore illegal in some Operating systems.
     * SHA256 is used specifically for it's balance between security and length and it's wide adoption. The result is then encoded in B64.
     * I know that this does drastically reduces security under case invariant filesystems.
     * Though the safety is still on the level of SHA224 with tha, which is enough for now.
     * @param remoteResource the URL to encode into the filename
     * @return B64 encoded Sha256 hash of input
     */
    private String toOsCompliantFileName(URL remoteResource) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assert md != null; //Safe, as any Java platform is REQUIRED to provide SHA256
        return Base64.getUrlEncoder().encodeToString(md.digest(remoteResource.toString().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Ensures that a given resource is cached under a given path
     * @param remoteResource the resource to check
     * @param localCache the local to check
     * @param noUpdate whether to prevent the cache from updating, if a new Version of the resource is available
     * @throws IOException when an error occurred fetching  the resource
     */
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
