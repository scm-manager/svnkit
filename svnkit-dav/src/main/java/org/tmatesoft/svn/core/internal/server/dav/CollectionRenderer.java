/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */



package org.tmatesoft.svn.core.internal.server.dav;

//~--- non-JDK imports --------------------------------------------------------

import org.tmatesoft.svn.core.SVNException;

/**
 * @author Sebastian Sdorra
 */
public interface CollectionRenderer
{

  /**
   * Render html for collection.
   *
   *
   * @param builder
   * @param resource
   *
   * @throws SVNException
   */
  public void renderCollection(StringBuilder builder, DAVResource resource)
    throws SVNException;
}
