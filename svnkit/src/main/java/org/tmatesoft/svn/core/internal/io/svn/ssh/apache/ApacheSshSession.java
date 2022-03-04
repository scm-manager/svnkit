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
package org.tmatesoft.svn.core.internal.io.svn.ssh.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.tmatesoft.svn.core.internal.io.svn.ssh.SshSession;

public class ApacheSshSession implements SshSession {
    private static final Logger log = Logger.getLogger(ApacheSshSession.class.getName());
    private final SshConnection connection;
    private ChannelExec channel;
    private PipedInputStream out;
    private PipedInputStream err;
    private PipedOutputStream in;

    public ApacheSshSession(SshConnection connection) {
        this.connection = connection;
    }
    
    public void close() {
        if (channel != null) {
            try {
                channel.close();
                channel.waitFor(Collections.singleton(ClientChannelEvent.CLOSED), 10000);
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to close channel", e);
            }
        }
//        waitForCondition(ChannelCondition.CLOSED, 0);
        connection.sessionClosed(this);
    }    
    
    public InputStream getOut() {
        if (in == null) {
            throw new IllegalStateException("execCommand must be called first");
        }
        return out;
    }

    public InputStream getErr() {
        if (in == null) {
            throw new IllegalStateException("execCommand must be called first");
        }
        return err;
    }
    
    public OutputStream getIn() {
        if (in == null) {
            throw new IllegalStateException("execCommand must be called first");
        }
        return in;
    }

    @Override
    public void waitForCondition(int code, long timeout) {
        // TODO
    }

    public void execCommand(String command) throws IOException {
        channel = connection.getSession().createExecChannel(command);
        out = new PipedInputStream();
        channel.setOut(new PipedOutputStream(out));
        err = new PipedInputStream();
        channel.setErr(new PipedOutputStream(err));
        in = new PipedOutputStream();
        channel.setIn(new PipedInputStream(in));
        channel.open().await();
        log.info("Opened connection with "+command);
    }
    
    public void ping() throws IOException {
        // TODO
    }
}
