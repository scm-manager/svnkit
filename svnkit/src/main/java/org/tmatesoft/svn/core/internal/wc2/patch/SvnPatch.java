import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.wc2.ng.SvnDiffGenerator;
            new Transition("--- a/", ParserState.GIT_MODE_SEEN, IParserFunction.GIT_MINUS),
            new Transition("--- /dev/null", ParserState.GIT_MODE_SEEN, IParserFunction.GIT_MINUS),
            new Transition("old mode ", ParserState.GIT_DIFF_SEEN, IParserFunction.GIT_OLD_MODE),
            new Transition("new mode ", ParserState.OLD_MODE_SEEN, IParserFunction.GIT_NEW_MODE),
            new Transition("rename from ", ParserState.GIT_MODE_SEEN, IParserFunction.GIT_MOVE_FROM),
            new Transition("copy from ", ParserState.GIT_MODE_SEEN, IParserFunction.GIT_COPY_FROM),
            new Transition("deleted file ", ParserState.GIT_DIFF_SEEN, IParserFunction.GIT_DELETED_FILE),
            new Transition("index ", ParserState.GIT_DIFF_SEEN, IParserFunction.GIT_INDEX),
            new Transition("index ", ParserState.GIT_TREE_SEEN, IParserFunction.GIT_INDEX),
            new Transition("index ", ParserState.GIT_MODE_SEEN, IParserFunction.GIT_INDEX),
            new Transition("GIT binary patch", ParserState.GIT_DIFF_SEEN, IParserFunction.BINARY_PATCH_START),
            new Transition("GIT binary patch", ParserState.GIT_TREE_SEEN, IParserFunction.BINARY_PATCH_START),
            new Transition("GIT binary patch", ParserState.GIT_MODE_SEEN, IParserFunction.BINARY_PATCH_START)
        patch.setNodeKind(SVNNodeKind.UNKNOWN);
        patch.setOperation(SvnDiffCallback.OperationKind.Unchanged);
                    state = transition.getParserFunction().parse(line, patch, state);
            if (state == ParserState.UNIDIFF_FOUND || state == ParserState.GIT_HEADER_FOUND || state == ParserState.BINARY_PATCH_FOUND) {
            } else if ((state == ParserState.GIT_TREE_SEEN || state == ParserState.GIT_MODE_SEEN) && lineAfterTreeHeaderRead && !validHeaderLine) {
                patchFile.getPatchFileStream().setSeekPosition(lastLine);
                break;
            } else if (state == ParserState.GIT_TREE_SEEN || state == ParserState.GIT_MODE_SEEN) {
                    && state != ParserState.GIT_DIFF_SEEN) {

            switch (patch.getOperation()) {
                case Added:
                    patch.setOperation(SvnDiffCallback.OperationKind.Deleted);
                    break;
                case Deleted:
                    patch.setOperation(SvnDiffCallback.OperationKind.Added);
                    break;
                case Modified:
                    break;
                case Copied:
                case Moved:
                    break;
                case Unchanged:
                    break;
            }

            Boolean tmpBit = patch.getOldExecutableBit();
            patch.setOldExecutableBit(patch.getNewExecutableBit());
            patch.setNewExecutableBit(tmpBit);

            tmpBit = patch.getOldSymlinkBit();
            patch.setOldSymlinkBit(patch.getNewSymlinkBit());
            patch.setNewSymlinkBit(tmpBit);
            if (state == ParserState.BINARY_PATCH_FOUND) {
                patch.parseBinaryPatch(patch, patchFile.getPatchFileStream(), reverse);
            }
    private BinaryPatch binaryPatch;

    private Boolean newExecutableBit; //tristate: true/false/unknown
    private Boolean oldExecutableBit; //tristate: true/false/unknown

    private Boolean newSymlinkBit; //tristate: true/false/unknown
    private Boolean oldSymlinkBit; //tristate: true/false/unknown

    private SVNNodeKind nodeKind;
    private void parseBinaryPatch(SvnPatch patch, SVNPatchFileStream patchFileStream, boolean reverse) throws IOException, SVNException {
        boolean eof = false;
        boolean inBlob = false;
        boolean inSrc = false;

        final BinaryPatch binaryPatch = new BinaryPatch();
        binaryPatch.setPatchFileStream(patchFileStream);

        long lastLine = -1;
        long pos = patchFileStream.getSeekPosition();
        while (!eof) {
            lastLine = pos;

            StringBuffer lineBuffer = new StringBuffer();
            eof = patchFileStream.readLine(lineBuffer);
            String line = lineBuffer.toString();

            pos = patchFileStream.getSeekPosition();

            if (inBlob) {
                char c = line.length() == 0 ? '\0' : line.charAt(0);
                if (((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
                        && line.length() <= 66
                        && !line.contains(":")
                        && !line.contains(" ")) {
                    if (inSrc) {
                        binaryPatch.setSrcEnd(pos);
                    } else {
                        binaryPatch.setDstEnd(pos);
                    }
                } else if (containsNonSpaceCharacter(line) && !(inSrc && binaryPatch.getSrcStart() < lastLine)) {
                    //bad patch
                    break;
                } else if (inSrc) {
                    //success
                    patch.setBinaryPatch(binaryPatch);
                    break;
                } else {
                    inBlob = false;
                    inSrc = true;
                }
            } else if (line.startsWith("literal ")) {
                try {
                    long expandedSize = Long.parseLong(line.substring("literal ".length()));

                    if (inSrc) {
                        binaryPatch.setSrcStart(pos);
                        binaryPatch.setSrcFileSize(expandedSize);
                    } else {
                        binaryPatch.setDstStart(pos);
                        binaryPatch.setDstFileSize(expandedSize);
                    }
                    inBlob = true;
                } catch (NumberFormatException e) {
                    break;
                }
            } else {
                //Git deltas are not supported
                break;
            }
        }
        if (!eof) {
            patchFileStream.setSeekPosition(lastLine);
        } else if (inSrc && ((binaryPatch.getSrcEnd() > binaryPatch.getSrcStart()) || (binaryPatch.getSrcFileSize() == 0))) {
            //success
            patch.setBinaryPatch(binaryPatch);
        }

        if (reverse && (patch.getBinaryPatch() != null)) {
            long tmpStart = binaryPatch.getSrcStart();
            long tmpEnd = binaryPatch.getSrcEnd();
            long tmpFileSize = binaryPatch.getSrcFileSize();

            binaryPatch.setSrcStart(binaryPatch.getDstStart());
            binaryPatch.setSrcEnd(binaryPatch.getDstEnd());
            binaryPatch.setSrcFileSize(binaryPatch.getDstFileSize());

            binaryPatch.setDstStart(tmpStart);
            binaryPatch.setDstEnd(tmpEnd);
            binaryPatch.setDstFileSize(tmpFileSize);
        }
    }

    private boolean containsNonSpaceCharacter(String line) {
        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (!Character.isSpaceChar(c)) {
                return true;
            }
        }
        return false;
    }

        boolean originalNoFinalEol = false;
        boolean modifiedNoFinalEol = false;


                    if (lastLineType != LineType.MODIFIED_LINE) {
                        originalNoFinalEol = true;
                    }
                    if (lastLineType != LineType.ORIGINAL_LINE) {
                        modifiedNoFinalEol = true;
                    }
                } else if (c == del && (originalLines > 0 || line.charAt(1) != del)) {
                    if (originalLines > 0) {
                        originalLines--;
                    } else {
                        hunk.setOriginalLength(hunk.getOriginalLength() + 1);
                        hunk.setOriginalFuzz(hunk.getOriginalFuzz() + 1);
                    }
                    lastLineType = LineType.ORIGINAL_LINE;
                } else if (c == add && (modifiedLines > 0 || line.charAt(1) != add)) {
                    if (modifiedLines > 0) {
                        modifiedLines--;
                    } else {
                        hunk.setModifiedLength(hunk.getModifiedLength() + 1);
                        hunk.setModifiedFuzz(hunk.getModifiedFuzz() + 1);
                    }
            if (originalLines != 0) {
                hunk.setOriginalLength(hunk.getOriginalLength() - originalLines);
                hunk.setOriginalFuzz(hunk.getOriginalFuzz() + originalLines);
            }
            if (modifiedLines != 0) {
                hunk.setModifiedLength(hunk.getModifiedLength() - modifiedLines);
                hunk.setModifiedFuzz(hunk.getModifiedFuzz() + modifiedLines);
            }

            hunk.setOriginalNoFinalEol(originalNoFinalEol);
            hunk.setModifiedNoFinalEol(modifiedNoFinalEol);
    public void setBinaryPatch(BinaryPatch binaryPatch) {
        this.binaryPatch = binaryPatch;
    }

    public BinaryPatch getBinaryPatch() {
        return binaryPatch;
    }

    public Boolean getNewExecutableBit() {
        return newExecutableBit;
    }

    public void setNewExecutableBit(Boolean newExecutableBit) {
        this.newExecutableBit = newExecutableBit;
    }

    public Boolean getOldExecutableBit() {
        return oldExecutableBit;
    }

    public void setOldExecutableBit(Boolean oldExecutableBit) {
        this.oldExecutableBit = oldExecutableBit;
    }

    public Boolean getNewSymlinkBit() {
        return newSymlinkBit;
    }

    public void setNewSymlinkBit(Boolean newSymlinkBit) {
        this.newSymlinkBit = newSymlinkBit;
    }

    public Boolean getOldSymlinkBit() {
        return oldSymlinkBit;
    }

    public void setOldSymlinkBit(Boolean oldSymlinkBit) {
        this.oldSymlinkBit = oldSymlinkBit;
    }

    public SVNNodeKind getNodeKind() {
        return nodeKind;
    }

    public void setNodeKind(SVNNodeKind nodeKind) {
        this.nodeKind = nodeKind;
    }

        START, GIT_DIFF_SEEN, GIT_TREE_SEEN, GIT_MINUS_SEEN, GIT_PLUS_SEEN, OLD_MODE_SEEN, GIT_MODE_SEEN, MOVE_FROM_SEEN, COPY_FROM_SEEN, MINUS_SEEN, UNIDIFF_FOUND, GIT_HEADER_FOUND, BINARY_PATCH_FOUND
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
                
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
        IParserFunction GIT_OLD_MODE = new IParserFunction() {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
                final Boolean[] modeBits = patch.parseGitModeBits(line.substring("old mode ".length()));
                patch.setOldExecutableBit(modeBits[0]);
                patch.setOldSymlinkBit(modeBits[1]);
                return ParserState.OLD_MODE_SEEN;
            }
        };
        IParserFunction GIT_NEW_MODE = new IParserFunction() {
            @Override
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
                final Boolean[] modeBits = patch.parseGitModeBits(line.substring("new mode ".length()));
                patch.setNewExecutableBit(modeBits[0]);
                patch.setNewSymlinkBit(modeBits[1]);
                return ParserState.GIT_MODE_SEEN;
            }
        };
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
                final Boolean[] modeBits = patch.parseGitModeBits(line.substring("new file mode ".length()));
                patch.setNewExecutableBit(modeBits[0]);
                patch.setNewSymlinkBit(modeBits[1]);

                patch.setNodeKind(SVNNodeKind.FILE);
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
        IParserFunction GIT_INDEX = new IParserFunction() {
            @Override
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
                final int pos = line.substring("index ".length()).indexOf(' ');
                if (pos >= 0 &&
                        patch.getNewExecutableBit() == null &&
                        patch.getNewSymlinkBit() == null &&
                        patch.getOperation() != SvnDiffCallback.OperationKind.Added &&
                        patch.getOperation() != SvnDiffCallback.OperationKind.Deleted) {
                    final Boolean[] modeBits = patch.parseGitModeBits(line.substring(" ".length()));
                    patch.setNewExecutableBit(modeBits[0]);
                    patch.setNewSymlinkBit(modeBits[1]);
                    patch.setOldExecutableBit(patch.getNewExecutableBit());
                    patch.setOldSymlinkBit(patch.getNewSymlinkBit());
                }
                return currentState;
            }
        };
        IParserFunction BINARY_PATCH_START = new IParserFunction() {
            @Override
            public ParserState parse(String line, SvnPatch patch, ParserState currentState) {
                return ParserState.BINARY_PATCH_FOUND;
            }
        };
        ParserState parse(String line, SvnPatch patch, ParserState currentState);
    }

    @SuppressWarnings("OctalInteger")
    private Boolean[] parseGitModeBits(String modeString) {
        Boolean executableBit;
        Boolean symlinkBit;
        //this method should assign newExecutableBit and newSymlinkBit
        final int mode = Integer.parseInt(modeString, 8);
        switch (mode  & 0777) {
            case 0644:
                executableBit = Boolean.FALSE;
                break;
            case 0755:
                executableBit = Boolean.TRUE;
                break;
            default:
                executableBit = null;
                break;
        }
        switch (mode & 0170000) {
            case 0120000:
                symlinkBit = Boolean.TRUE;
                break;
            case 0100000:
            case 0040000:
                symlinkBit = Boolean.FALSE;
                break;
            default:
                symlinkBit = null;
                break;
        }
        return new Boolean[] {executableBit, symlinkBit};

    public static class BinaryPatch {
        private SvnPatch patch;
        private SVNPatchFileStream patchFileStream;
        private long srcStart;
        private long srcEnd;
        private long srcFileSize;
        private long dstStart;
        private long dstEnd;
        private long dstFileSize;

        public SvnPatch getPatch() {
            return patch;
        }

        public void setPatch(SvnPatch patch) {
            this.patch = patch;
        }

        public SVNPatchFileStream getPatchFileStream() {
            return patchFileStream;
        }

        public void setPatchFileStream(SVNPatchFileStream patchFileStream) {
            this.patchFileStream = patchFileStream;
        }

        public long getSrcStart() {
            return srcStart;
        }

        public void setSrcStart(long srcStart) {
            this.srcStart = srcStart;
        }

        public long getSrcEnd() {
            return srcEnd;
        }

        public void setSrcEnd(long srcEnd) {
            this.srcEnd = srcEnd;
        }

        public long getSrcFileSize() {
            return srcFileSize;
        }

        public void setSrcFileSize(long srcFileSize) {
            this.srcFileSize = srcFileSize;
        }

        public long getDstStart() {
            return dstStart;
        }

        public void setDstStart(long dstStart) {
            this.dstStart = dstStart;
        }

        public long getDstEnd() {
            return dstEnd;
        }

        public void setDstEnd(long dstEnd) {
            this.dstEnd = dstEnd;
        }

        public long getDstFileSize() {
            return dstFileSize;
        }

        public void setDstFileSize(long dstFileSize) {
            this.dstFileSize = dstFileSize;
        }

        public InputStream getBinaryDiffOriginalStream() {
            InputStream inputStream = new Base85DataStream(patchFileStream, srcStart, srcEnd);
            inputStream = new InflaterInputStream(inputStream);

            return new CheckBase85LengthInputStream(inputStream, srcFileSize);
        }

        public InputStream getBinaryDiffResultStream() {
            InputStream inputStream = new Base85DataStream(patchFileStream, dstStart, dstEnd);
            inputStream = new InflaterInputStream(inputStream);

            return new CheckBase85LengthInputStream(inputStream, dstFileSize);
        }
    }

    private static class Base85DataStream extends InputStream {

        private final SVNPatchFileStream patchFileStream;
        private long start;
        private final long end;

        private boolean done;
        private byte[] buffer;
        private int bufSize;
        private int bufPos;
        private byte[] singleByteBuffer;

        public Base85DataStream(SVNPatchFileStream patchFileStream, long start, long end) {
            this.patchFileStream = patchFileStream;
            this.start = start;
            this.end = end;
            this.done = false;
            this.buffer = new byte[52];
            this.bufSize = 0;
            this.bufPos = 0;
            this.singleByteBuffer = new byte[1];
        }

        @Override
        public int read() throws IOException {
            final int bytesRead = read(singleByteBuffer, 0, 1);
            if (bytesRead < 0) {
                return bytesRead;
            } else {
                return singleByteBuffer[0] & 0xff;
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                int remaining = len;
                int destOff = off;
                if (done) {
                    return -1;
                }

                while (remaining != 0 && (bufSize > bufPos || start < end)) {
                    boolean atEof;

                    int available = bufSize - bufPos;
                    if (available != 0) {
                        int n = (remaining < available) ? remaining : available;

                        System.arraycopy(buffer, bufPos, b, destOff, n);
                        destOff += n;
                        remaining -= n;
                        bufPos += n;

                        if (remaining == 0) {
                            return len;
                        }
                    }

                    if (start >= end) {
                        break;
                    }
                    patchFileStream.setSeekPosition(start);
                    final StringBuffer lineBuf = new StringBuffer();
                    atEof = patchFileStream.readLine(lineBuf);
                    final String line = lineBuf.toString();

                    if (atEof) {
                        start = end;
                    } else {
                        start = patchFileStream.getSeekPosition();
                    }
                    if (line.length() > 0 && line.charAt(0) >= 'A' && line.charAt(0) <= 'Z') {
                        bufSize = line.charAt(0) - 'A' + 1;
                    } else if (line.length() > 0 && line.charAt(0) >= 'a' && line.charAt(0) <= 'z') {
                        bufSize = line.charAt(0) - 'a' + 26 + 1;
                    } else {
                        throw new IOException("Unexpected data in base85 section");
                    }
                    if (bufSize < 52) {
                        start = end;
                    }
                    base85DecodeLine(buffer, bufSize, line.substring(1));
                    bufPos = 0;
                }

                len -= remaining;
                done = true;

                return len;
            } catch (SVNException e) {
                throw new IOException(e);
            }
        }

        private static void base85DecodeLine(byte[] outputBuffer, int outputBufferSize, String line) throws IOException {
            int expectedData = (outputBufferSize + 3) / 4 * 5;
            if (line.length() != expectedData) {
                throw new IOException("Unexpected base85 line length");
            }
            int base85Offet = 0;
            int base85Length = line.length();
            int outputBufferOffset = 0;
            while (base85Length != 0) {
                long info = 0;

                for (int i = 0; i < 5; i++) {
                    int value = base85Value(line.charAt(base85Offet + i));
                    info *= 85;
                    info += value;
                }
                for (int i = 0, n = 24; i < 4; i++, n -= 8) {
                    if (i < outputBufferSize) {
                        outputBuffer[outputBufferOffset + i] = (byte) ((info >> n) & 0xFF);
                    }
                }
                base85Offet += 5;
                base85Length -= 5;
                outputBufferOffset += 4;
                outputBufferSize -= 4;
            }
        }

        private static int base85Value(char c) throws IOException {
            final int index = SvnDiffGenerator.B85_TABLE.indexOf(String.valueOf(c));
            if (index < 0) {
                throw new IOException("Invalid base85 value");
            }
            return index;
        }
    }
    private static class CheckBase85LengthInputStream extends InputStream {

        private final InputStream inputStream;
        private final byte[] singleByteBuffer;

        private long remaining;

        private CheckBase85LengthInputStream(InputStream inputStream, long remaining) {
            this.inputStream = inputStream;
            this.remaining = remaining;
            this.singleByteBuffer = new byte[1];
        }

        @Override
        public int read() throws IOException {
            final int read = inputStream.read(singleByteBuffer, 0, 1);
            if (read < 0) {
                return read;
            } else {
                return singleByteBuffer[0] & 0xff;
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int requestedLength = len;
            len = readFully(inputStream, b, off, len);
            if (len < 0) {
                // <0 means stream finished, so we've read 0 bytes
                len = 0;
            }

            if (len > remaining) {
                throw new IOException("Base85 data expands to longer than declared filesize");
            } else if (requestedLength > len && len != remaining) {
                throw new IOException("Base85 data expands to smaller than declared filesize");
            }
            remaining -= len;
            return len == 0 ? -1 : len;
        }

        @Override
        public long skip(long n) throws IOException {
            return inputStream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return inputStream.available();
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }

        @Override
        public void mark(int readlimit) {
            inputStream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            inputStream.reset();
        }

        @Override
        public boolean markSupported() {
            return inputStream.markSupported();
        }
    }

    protected static int readFully(InputStream inputStream, byte[] b, int off, int len) throws IOException {
        int totalBytesRead = 0;
        while (true) {
            final int bytesRead = inputStream.read(b, off, len);
            if (bytesRead < 0) {
                break;
            }
            totalBytesRead += bytesRead;
            off += bytesRead;
            len -= bytesRead;
        }
        if (totalBytesRead == 0) {
            return -1;
        }
        return totalBytesRead;
    }