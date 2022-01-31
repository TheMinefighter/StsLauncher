package de.theminefighter.stslauncher;

public class Flags {
    /**
     * used for offline debugging
     */
    static final boolean offline = false;
    /**
     * Whether to forward JVM props to the new STS process
     */
    static final boolean forwardProps = true;
    /**
     * Whether to forward the env of this process to the new java process
     */
    static final boolean forwardEnv = true;
    /**
     * Whether to forward the IO of the new STS process
     */
    static final boolean forwardSTSProcessIO = true;
    /**
     * Whether to set the new JVM Process to verbose output
     */
    static final boolean verboseJVM = false;
}
