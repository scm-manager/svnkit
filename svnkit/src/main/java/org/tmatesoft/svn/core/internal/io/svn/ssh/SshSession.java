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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Session, aka. an ssh channel
 */
public interface SshSession {
    void close();

    InputStream getOut();

    InputStream getErr();

    OutputStream getIn();

    void waitForCondition(int code, long timeout);

    void execCommand(String command) throws IOException;

    void ping() throws IOException;
}
