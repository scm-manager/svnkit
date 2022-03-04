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

public class SshAuthenticationException extends IOException {

    private static final long serialVersionUID = 1L;
    
    public SshAuthenticationException(String message) {
        super(message);
    }

}
