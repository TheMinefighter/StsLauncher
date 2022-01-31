package de.theminefighter.stslauncher.caching;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

/**
 * Basic caching interface
 */
public interface ResourceCache {
    /**
     * Gets a resource from the cache
     * @param remoteResource the resource to cache
     * @param noUpdate whether to ignore updates on th resource, may be ignore
     * @return the URL of the cached resource
     */
	 File get(URL remoteResource, boolean noUpdate) ;
	 File storeLocal(File file,URL id);
}
