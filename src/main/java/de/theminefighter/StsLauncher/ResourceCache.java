package de.theminefighter.StsLauncher;

import java.io.IOException;
import java.net.URL;

public interface ResourceCache {
    public URL get(URL remoteResource, boolean noUpdate) ;
}
