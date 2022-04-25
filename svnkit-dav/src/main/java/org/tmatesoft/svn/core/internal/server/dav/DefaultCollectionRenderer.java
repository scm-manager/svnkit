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

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.util.Version;

import java.util.Iterator;

/**
 * @author Sebastian Sdorra
 */
public class DefaultCollectionRenderer implements CollectionRenderer
{

  @Override
  public void renderCollection(StringBuilder buffer, DAVResource resource) throws SVNException {
    startBody(SVNPathUtil.tail(resource.getResourceURI().getContext()), resource.getResourceURI().getPath(), resource.getRevision(), buffer);
    addUpperDirectoryLink(resource.getResourceURI().getContext(), resource.getResourceURI().getPath(), buffer);
    addDirectoryEntries(resource, buffer);
    finishBody(buffer);
  }

  private void startBody(String contextComponent, String path, long revision, StringBuilder buffer) {
    buffer.append("<html><head><title>");
    buffer.append(contextComponent);
    buffer.append(" - Revision ");
    buffer.append(String.valueOf(revision));
    buffer.append(": ");
    buffer.append(path);
    buffer.append("</title></head>\n");
    buffer.append("<body>\n<h2>");
    buffer.append(contextComponent);
    buffer.append(" - Revision ");
    buffer.append(String.valueOf(revision));
    buffer.append(": ");
    buffer.append(path);
    buffer.append("</h2>\n <ul>\n");
  }

  private void addUpperDirectoryLink(String context, String path, StringBuilder buffer) {
    if (!"/".equals(path)) {
      buffer.append("<li><a href=\"");
      buffer.append(context);
      String parent = DAVPathUtil.removeTail(path, true);
      buffer.append("/".equals(parent) ? "" : parent);
      buffer.append("/");
      buffer.append("\">..</a></li>\n");
    }
  }

  private void addDirectoryEntries(DAVResource resource, StringBuilder buffer) throws SVNException {
    for (Iterator iterator = resource.getEntries().iterator(); iterator.hasNext();) {
      SVNDirEntry entry = (SVNDirEntry) iterator.next();
      boolean isDir = entry.getKind() == SVNNodeKind.DIR;
      buffer.append("<li><a href=\"");
      buffer.append(resource.getResourceURI().getContext());
      buffer.append("/".equals(resource.getResourceURI().getPath()) ? "" : resource.getResourceURI().getPath());
      buffer.append(DAVPathUtil.standardize(entry.getName()));
      buffer.append(isDir ? "/" : "");
      buffer.append("\">");
      buffer.append(entry.getName());
      buffer.append(isDir ? "/" : "");
      buffer.append("</a></li>\n");
    }
  }

  private void finishBody(StringBuilder buffer) {
    buffer.append("</ul><hr noshade><em>");
    buffer.append("Powered by ");
    buffer.append(Version.getVersionString());
    buffer.append("</em>\n</body></html>");
  }
}
