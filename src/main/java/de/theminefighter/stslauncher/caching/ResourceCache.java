package de.theminefighter.stslauncher.caching;

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
	 URL get(URL remoteResource, boolean noUpdate) ;
}
