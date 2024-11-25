package de.theminefighter.stslauncher.caching;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

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

    /**
     * Stores a given file in the local cache to be found under the given URL
     * @param file the file to store
     * @param id the id to store it under
     * @return the file stored
     */
	 File storeLocal(File file,URL id);

	/**
	 * Obtains a stream to fill a cache entry
	 * @param id the url of the cache entry to access
	 * @return an OutputStream that the caller MUST close
	 */
	OutputStream writeStream(URL id);

	/**
	 * Checks whether the cache contains an entry for a given URL
	 * @param id the URL to check for
	 * @return whether it is cached
	 */
	boolean has(URL id);
}
