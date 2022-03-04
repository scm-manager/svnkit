/*
 * ====================================================================
 * Copyright (c) 2004-2022 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.io.svn.ssh;

import java.util.logging.Logger;

import org.tmatesoft.svn.core.internal.io.svn.ssh.apache.ApacheSshSessionPool;
import org.tmatesoft.svn.core.internal.io.svn.ssh.trilead.TrileadSshSessionPool;

/**
 * This is where SshSessionPool instances are born
 */
public class SessionPoolFactory {
    private static final Logger log = Logger.getLogger(SessionPoolFactory.class.getName());
    public static final String TRILEAD = "trilead";
    public static final String SVNKIT_SSH_CLIENT = "svnkit.ssh.client";
    public static final String APACHE = "apache";

    public static SshSessionPool create() {
        final String implementationName = System.getProperty(SVNKIT_SSH_CLIENT, TRILEAD);

        if (implementationName.equals(TRILEAD)) {
            log.warning("Using the obsolete " + TRILEAD + " ssh client implementation, consider switching to " + APACHE +
                    " by using -D" + SVNKIT_SSH_CLIENT + "=" + APACHE);
            return new TrileadSshSessionPool();
        } else {
            return new ApacheSshSessionPool();
        }
    }
}
