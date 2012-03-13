package org.tmatesoft.svn.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.db.SVNSqlJetDb;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb;
import org.tmatesoft.svn.core.internal.wc17.db.SVNWCDb;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;

public class WorkingCopyRootDetectionTest {
    @Test
    public void testBasics() throws Exception {
        final TestOptions options = TestOptions.getInstance();

        final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
        final Sandbox sandbox = Sandbox.createWithCleanup(getTestName() + ".testBasics", options);
        try {
            final SVNURL url = sandbox.createSvnRepository();

            final CommitBuilder commitBuilder = new CommitBuilder(url);
            commitBuilder.addDirectory("directory/subdirectory/subsubdirectory");
            commitBuilder.commit();

            final WorkingCopy workingCopy = sandbox.checkoutNewWorkingCopy(url, SVNRevision.HEAD.getNumber());
            final File workingCopyDirectory = workingCopy.getWorkingCopyDirectory();

            final SVNWCDb db = createSVNWCDb();
            db.open(ISVNWCDb.SVNWCDbOpenMode.ReadOnly, SVNWCUtil.createDefaultOptions(true), false, true);
            try {
                assertRootIsCorrect(workingCopyDirectory, db, workingCopyDirectory);
                assertRootIsCorrect(workingCopyDirectory, db, new File(workingCopyDirectory, "directory"));
                assertRootIsCorrect(workingCopyDirectory, db, new File(workingCopyDirectory, "directory/subdirectory"));
                assertRootIsCorrect(workingCopyDirectory, db, new File(workingCopyDirectory, "directory/subdirectory/subsubdirectory"));
            } finally {
                db.close();
            }
        } finally {
            svnOperationFactory.dispose();
            sandbox.dispose();
        }
    }

    @Test
    public void testSymlinkToWorkingCopyInsideWorkingCopy() throws Exception {
        Assume.assumeTrue(SVNFileUtil.symlinksSupported());
        final TestOptions options = TestOptions.getInstance();

        final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
        final Sandbox sandbox = Sandbox.createWithCleanup(getTestName() + ".testSymlinkToWorkingCopyInsideWorkingCopy", options);
        try {
            final SVNURL url = sandbox.createSvnRepository();

            final CommitBuilder commitBuilder = new CommitBuilder(url);
            commitBuilder.addFile("symlink", "link symlinkTarget".getBytes());
            commitBuilder.setFileProperty("symlink", SVNProperty.SPECIAL, SVNPropertyValue.create("*"));
            commitBuilder.commit();

            final WorkingCopy workingCopy = sandbox.checkoutNewWorkingCopy(url, SVNRevision.HEAD.getNumber());
            final File workingCopyDirectory = workingCopy.getWorkingCopyDirectory();

            final File symlink = new File(workingCopyDirectory, "symlink");
            final File symlinkTarget = new File(workingCopyDirectory, "symlinkTarget");

            SVNWCDb db;

            db = createSVNWCDb();
            db.open(ISVNWCDb.SVNWCDbOpenMode.ReadOnly, SVNWCUtil.createDefaultOptions(true), false, true);
            try {
                assertRootIsCorrect(workingCopyDirectory, db, symlink);
                assertRootIsCorrect(workingCopyDirectory, db, symlinkTarget);
            } finally {
                db.close();
            }

            final WorkingCopy symlinkTargetWorkingCopy = new WorkingCopy(options, symlinkTarget);
            symlinkTargetWorkingCopy.checkoutLatestRevision(url);

            db = createSVNWCDb();
            db.open(ISVNWCDb.SVNWCDbOpenMode.ReadOnly, SVNWCUtil.createDefaultOptions(true), false, true);
            try {
                assertRootIsCorrect(workingCopyDirectory, db, symlink); //symlink still belongs to the parent repository
                assertRootIsCorrect(symlinkTarget, db, symlinkTarget);
            } finally {
                db.close();
            }
        } finally {
            svnOperationFactory.dispose();
            sandbox.dispose();
        }
    }

    @Test
    public void testSymlinkToWorkingCopyBeyondWorkingCopy() throws Exception {
        Assume.assumeTrue(SVNFileUtil.symlinksSupported());
        final TestOptions options = TestOptions.getInstance();

        final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
        final Sandbox sandbox = Sandbox.createWithCleanup(getTestName() + ".testSymlinkToWorkingCopyBeyondWorkingCopy", options);
        try {
            final SVNURL url = sandbox.createSvnRepository();

            final CommitBuilder commitBuilder = new CommitBuilder(url);
            commitBuilder.addDirectory("versionedDirectory");
            commitBuilder.commit();

            final WorkingCopy workingCopy = sandbox.checkoutNewWorkingCopy(url, SVNRevision.HEAD.getNumber());
            final File workingCopyDirectory = workingCopy.getWorkingCopyDirectory();

            final File symlink = new File(sandbox.createDirectory("directory"), "symlink");
            SVNFileUtil.createSymlink(symlink, workingCopyDirectory.getAbsolutePath());

            SVNWCDb db;

            db = createSVNWCDb();
            db.open(ISVNWCDb.SVNWCDbOpenMode.ReadOnly, SVNWCUtil.createDefaultOptions(true), false, true);
            try {
                assertRootIsCorrect(symlink, db, symlink);
                assertRootIsCorrect(symlink, db, new File(symlink, "versionedDirectory"));
            } finally {
                db.close();
            }
        } finally {
            svnOperationFactory.dispose();
            sandbox.dispose();
        }
    }

    private void assertRootIsCorrect(File expectedRoot, SVNWCDb db, File path) throws SVNException {
        expectedRoot = new File(expectedRoot.getAbsolutePath().replace(File.separatorChar, '/'));
        path = new File(path.getAbsolutePath().replace(File.separatorChar, '/'));

        final SVNWCDb.DirParsedInfo dirParsedInfo = db.parseDir(path, SVNSqlJetDb.Mode.ReadOnly);
        final File actualRoot = dirParsedInfo.wcDbDir.getWCRoot().getAbsPath();
        Assert.assertEquals(expectedRoot, actualRoot);
    }

    private SVNWCDb createSVNWCDb() {
        return new SVNWCDb();
    }

    private String getTestName() {
        return "WorkingCopyRootDetectionTest";
    }
}
