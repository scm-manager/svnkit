/*
 * ====================================================================
 * Copyright (c) 2004-2011 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.io.fs;

import java.io.File;


/**
 * @author Sebastian Sdorra
 */
public class FSHookEvent 
{
    private String type;
    private File reposRootDir;
    private String[] args;
    
    public FSHookEvent(String type, File reposRootDir, String[] args) {
        this.type = type;
        this.reposRootDir = reposRootDir;
        this.args = args;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setReposRootDir(File reposRootDir) {
        this.reposRootDir = reposRootDir;
    }

    public File getReposRootDir() {
        return reposRootDir;
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public void setArgs(String[] args) {
        this.args = args;
    }
    
}
