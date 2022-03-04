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
package org.tmatesoft.svn.core.internal.io.svn.ssh.trilead;

import java.io.IOException;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;

public class SshConnection {
    
    private Connection myConnection;
    private volatile int mySessionCount;
    private SshHost myHost;
    private long myLastAccessTime;
    
    public SshConnection(SshHost host, Connection connection) {
        myHost = host;
        myConnection = connection;
        myLastAccessTime = System.currentTimeMillis();
    }
    
    public TrileadSshSession openSession() throws IOException {
        Session session = myConnection.openSession();
        if (session != null) {
            mySessionCount++;
            return new TrileadSshSession(this, session);
        }
        return null;
    }

    public void sessionClosed(TrileadSshSession sshSession) {
        myHost.lock();
        try {
            myLastAccessTime = System.currentTimeMillis();
            mySessionCount--;
        } finally {
            myHost.unlock();
        }
    }
    
    public int getSessionsCount() {
        return mySessionCount;
    }

    public void close() {
        myConnection.close();
        mySessionCount = 0;
    }

    public long lastAcccessTime() {
        return myLastAccessTime;
    }

}
