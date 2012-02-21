package org.tmatesoft.svn.core.internal.wc2.old;

import java.io.File;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.internal.wc16.SVNCommitClient16;
import org.tmatesoft.svn.core.internal.wc2.ISvnCommitRunner;
import org.tmatesoft.svn.core.internal.wc2.compat.SvnCodec;
import org.tmatesoft.svn.core.wc.ISVNCommitHandler;
import org.tmatesoft.svn.core.wc.SVNCommitItem;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc2.SvnCommit;
import org.tmatesoft.svn.core.wc2.SvnCommitPacket;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class SvnOldCommit extends SvnOldRunner<SVNCommitInfo, SvnCommit> implements ISvnCommitRunner, ISVNCommitHandler {

    public SvnCommitPacket collectCommitItems(SvnCommit operation) throws SVNException {
        setOperation(operation);
        SVNCommitClient16 client = new SVNCommitClient16(getOperation().getRepositoryPool(), getOperation().getOptions());
        client.setEventHandler(getOperation().getEventHandler());
        client.setCommitHandler(this);
        client.setCommitParameters(SvnCodec.commitParameters(getOperation().getCommitParameters()));

        File[] paths = new File[getOperation().getTargets().size()];
        int i = 0;
        for (SvnTarget tgt : getOperation().getTargets()) {
            paths[i++] = tgt.getFile();
        }
        
        String[] changelists = null;
        if (getOperation().getApplicableChangelists() != null && !getOperation().getApplicableChangelists().isEmpty()) {
            changelists = getOperation().getApplicableChangelists().toArray(new String[getOperation().getApplicableChangelists().size()]);
        }
        SVNCommitPacket packet = client.doCollectCommitItems(paths, getOperation().isKeepLocks(), getOperation().isForce(), getOperation().getDepth(), changelists);
        return SvnCodec.commitPacket(this, packet);
    }

    @Override
    protected SVNCommitInfo run() throws SVNException {
        SvnCommitPacket packet = getOperation().collectCommitItems();
        SVNCommitPacket oldPacket = (SVNCommitPacket) packet.getLockingContext();
        
        SVNCommitClient16 client = new SVNCommitClient16(getOperation().getRepositoryPool(), getOperation().getOptions());
        client.setEventHandler(getOperation().getEventHandler());
        client.setCommitHandler(this);
        client.setCommitParameters(SvnCodec.commitParameters(getOperation().getCommitParameters()));
        
        SVNCommitInfo info = client.doCommit(oldPacket, getOperation().isKeepLocks(), getOperation().isKeepChangelists(), getOperation().getCommitMessage(), getOperation().getRevisionProperties());
        if (info != null) {
            getOperation().receive(getOperation().getFirstTarget(), info);
        }
        return info;
    }

    public void disposeCommitPacket(Object lockingContext) throws SVNException {
        if (lockingContext instanceof SVNCommitPacket[]) {
            SVNCommitPacket[] packets = (SVNCommitPacket[]) lockingContext;
            for (int i = 0; i < packets.length; i++) {
                try {
                    packets[i].dispose();
                } catch (SVNException e) {
                    //
                }
            }
        }
    }

    public String getCommitMessage(String message, SVNCommitItem[] commitables) throws SVNException {
        return message;
    }

    public SVNProperties getRevisionProperties(String message, SVNCommitItem[] commitables, SVNProperties revisionProperties) throws SVNException {
        return revisionProperties;
    }

}
