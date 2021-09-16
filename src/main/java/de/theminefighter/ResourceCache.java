package de.theminefighter;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public interface ResourceCache {
    public URL updateAndGet(URL remoteResource) throws IOException;
}
