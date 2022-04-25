package org.tmatesoft.svn.core.internal.io.fs;

/**
 * Reads custom fs configuration settings from system properties.
 */
class CustomFSConfiguration {

    private boolean ignoreInvalidEncodedProperties = false;
    private boolean alwaysAllowBinaryProperties = false;

    private CustomFSConfiguration() {
        ignoreInvalidEncodedProperties = Boolean.getBoolean("sonia.svnkit.fsfs.ignoreInvalidEncodedProperties");
        alwaysAllowBinaryProperties = Boolean.getBoolean("sonia.svnkit.fsfs.alwaysAllowBinaryProperties");
    }

    /**
     * Returns {@code true} if every property should be treated as binary if the encoding fails.
     *
     * @return {@code true} if binary properties are always allowed
     */
    boolean isAlwaysAllowBinaryProperties() {
        return alwaysAllowBinaryProperties;
    }

    /**
     * Returns {@code true} if properties which are not decodable should be ignored.
     *
     * @return {@code true} if invalid encoded properties can be ignored
     */
    boolean isIgnoreInvalidEncodedProperties() {
        return ignoreInvalidEncodedProperties;
    }

    /**
     * Returns singleton instance.
     *
     * @return singleton instance
     */
    static CustomFSConfiguration getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final class InstanceHolder {
        static final CustomFSConfiguration INSTANCE = new CustomFSConfiguration();
    }

}
