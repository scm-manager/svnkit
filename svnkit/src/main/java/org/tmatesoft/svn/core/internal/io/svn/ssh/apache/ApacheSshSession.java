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
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.tmatesoft.svn.core.internal.io.svn.ssh.SshSession;

public class ApacheSshSession implements SshSession {

    private static final Logger log = Logger.getLogger(ApacheSshSession.class.getName());
    private SshConnection connection;
    private ChannelExec channel;
    private PipedInputStream out;
    private PipedInputStream err;
    private PipedOutputStream in;
    private static int execCount;

    public ApacheSshSession(SshConnection connection) {
        this.connection = connection;
    }

    public static int getExecCount() {
        return execCount;
    }

    public void close() {
        if (channel != null) {
            try {
                channel.close(false).await();
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
        execCount++;
        try {
            tryExecCommand(command);
        } catch (Exception e) {
            close();
            connection = connection.reOpen();
            tryExecCommand(command);
        }
    }

    private void tryExecCommand(String command) throws IOException {
        channel = connection.getSession().createExecChannel(command);
        out = new PipedInputStream();
        channel.setOut(new PipedOutputStream(out));
        err = new PipedInputStream();
        channel.setErr(new PipedOutputStream(err));
        in = new PipedOutputStream();
        channel.setIn(new PipedInputStream(in));

        final OpenFuture future = channel.open();
        future.await();
        if (!future.isOpened()) {
            Throwable exception = future.getException();
            if (exception != null) {
                throw new IOException(exception);
            } else {
                throw new IOException("Cannot open channel");
            }
        }
        log.finest(() -> "Opened connection with " + command);
    }

    /**
     * This doesn't really ping the server, because that's slow and useless anyway as we can silently re-connect to the server
     * in case we end up in a race-condition where the server has closed the connection between the call to ping() and
     * the call to execCommand()
     */
    public void ping() throws IOException {
        final ClientSession session = connection.getSession();
        if (!session.isOpen()) {
            throw new IOException("Session is not open");
        }

        if (session.isClosing()) {
            throw new IOException("Session is closing");
        }
    }
}
