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

import java.io.IOException;

import com.trilead.ssh2.auth.AgentProxy;
import org.tmatesoft.svn.core.auth.ISVNSSHHostVerifier;

/**
 * A pool of ssh clients that can open sessions
 */
public interface SshSessionPool {
    SshSession openSession(String host, int port, String userName,
                           char[] privateKey, char[] passphrase, char[] password, AgentProxy agentProxy,
                           ISVNSSHHostVerifier verifier, int connectTimeout, int readTimeout) throws IOException;
    void shutdown();
}
