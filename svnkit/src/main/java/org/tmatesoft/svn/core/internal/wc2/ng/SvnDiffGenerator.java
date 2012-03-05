import java.io.ByteArrayInputStream;
import org.tmatesoft.svn.core.wc.ISVNOptions;
    private String externalDiffCommand;
    private SVNDiffOptions diffOptions;
    private boolean fallbackToAbsolutePath;
    private ISVNOptions options;
        String relativePath;
        if (baseTarget == null) {
            relativePath = null;
        } else {
            String targetString = target.getPathOrUrlDecodedString();
            String baseTargetString = baseTarget.getPathOrUrlDecodedString();
            relativePath = SVNPathUtil.getRelativePath(baseTargetString, targetString);
        }

        return relativePath != null ? relativePath : target.getPathOrUrlString();
    }

    private String getRelativeToRootPath(SvnTarget target, SvnTarget originalTarget) {
        String relativePath;
        if (repositoryRoot == null) {
            relativePath = null;
        } else {
            if (repositoryRoot.isFile() == target.isFile()) {
                String targetString = target.getPathOrUrlDecodedString();
                String baseTargetString = repositoryRoot.getPathOrUrlDecodedString();
                relativePath = SVNPathUtil.getRelativePath(baseTargetString, targetString);
            } else {
                String targetString = target.getPathOrUrlDecodedString();
                String baseTargetString = new File("").getAbsolutePath();
                relativePath = SVNPathUtil.getRelativePath(baseTargetString, targetString);
            }
        }

        return relativePath != null ? relativePath : target.getPathOrUrlString();
    public void setBaseTarget(SvnTarget baseTarget) {
        this.baseTarget = baseTarget;
    }



            if (useGitFormat) {
                targetString1 = adjustRelativeToReposRoot(targetString1);
                targetString2 = adjustRelativeToReposRoot(targetString2);
            }

            String newTargetString = displayPath;
                displayGitDiffHeader(outputStream, SvnDiffCallback.OperationKind.Modified,
                        getRelativeToRootPath(target, originalTarget1),
                        getRelativeToRootPath(target, originalTarget2),
                        null);
//            if (useGitFormat) {
//                String copyFromPath = null;
//                SvnDiffCallback.OperationKind operationKind = SvnDiffCallback.OperationKind.Modified;
//                label1 = getGitDiffLabel1(operationKind, targetString1, targetString2, copyFromPath, revision1);
//                label2 = getGitDiffLabel2(operationKind, targetString1, targetString2, copyFromPath, revision2);
//                displayGitDiffHeader(outputStream, operationKind,
//                        getRelativeToRootPath(target, originalTarget1),
//                        getRelativeToRootPath(target, originalTarget2),
//                        copyFromPath);
//            }

                displayGitHeaderFields(outputStream, target, revision1, revision2, SvnDiffCallback.OperationKind.Modified, null);
            } else {
                displayHeaderFields(outputStream, label1, label2);
        displayPropertyChangesOn(useGitFormat ? getRelativeToRootPath(target, originalTarget1) : displayPath, outputStream);
    private void displayGitHeaderFields(OutputStream outputStream, SvnTarget target, String revision1, String revision2, SvnDiffCallback.OperationKind operation, String copyFromPath) throws SVNException {
        String path1 = copyFromPath != null ? copyFromPath : getRelativeToRootPath(target, originalTarget1);
        String path2 = getRelativeToRootPath(target, originalTarget2);

        try {
            displayString(outputStream, "--- ");
            displayFirstGitLabelPath(outputStream, path1, revision1, operation);
            displayEOL(outputStream);
            displayString(outputStream, "+++ ");
            displaySecondGitLabelPath(outputStream, path2, revision2, operation);
            displayEOL(outputStream);
        } catch (IOException e) {
            wrapException(e);
        }
    }

            return targetString + "\t(.../" + originalTargetString + ")";
            //TODO
                displayGitDiffHeader(outputStream, operation,
                        getRelativeToRootPath(target, originalTarget1),
                        getRelativeToRootPath(target, originalTarget2),
                        null);
                displayGitDiffHeader(outputStream, operation,
                        getRelativeToRootPath(target, originalTarget1),
                        getRelativeToRootPath(target, originalTarget2),
                        null);
            internalDiff(target, outputStream, displayPath, leftFile, rightFile, label1, label2, operation, copyFromPath == null ? null : copyFromPath.getPath(), revision1, revision2);
            displayMimeType(outputStream, mimeType1);
    private void internalDiff(SvnTarget target, OutputStream outputStream, String displayPath, File file1, File file2, String label1, String label2, SvnDiffCallback.OperationKind operation, String copyFromPath, String revision1, String revision2) throws SVNException {
        String header = getHeaderString(target, displayPath, operation, copyFromPath);
        String headerFields = getHeaderFieldsString(target, displayPath, label1, label2, revision1, revision2, operation, copyFromPath);
            properties.put(QDiffGeneratorFactory.IGNORE_EOL_PROPERTY, Boolean.valueOf(getDiffOptions().isIgnoreEOLStyle()));
            if (getDiffOptions().isIgnoreAllWhitespace()) {
            } else if (getDiffOptions().isIgnoreAmountOfWhitespace()) {
            if (forceEmpty || useGitFormat) {
    private String getHeaderFieldsString(SvnTarget target, String displayPath, String label1, String label2, String revision1, String revision2, SvnDiffCallback.OperationKind operation, String copyFromPath) throws SVNException {
                displayGitHeaderFields(byteArrayOutputStream, target, revision1, revision2, operation, copyFromPath);
            } else {
                displayHeaderFields(byteArrayOutputStream, label1, label2);
    private String getHeaderString(SvnTarget target, String displayPath, SvnDiffCallback.OperationKind operation, String copyFromPath) throws SVNException {
                displayGitDiffHeader(byteArrayOutputStream, operation,
                        getRelativeToRootPath(target, originalTarget1),
                        getRelativeToRootPath(target, originalTarget2),
                        copyFromPath);
            Collection svnDiffOptionsCollection = getDiffOptions().toOptionsCollection();
        return externalDiffCommand;

                byte[] originalValueBytes = getPropertyAsBytes(originalValue, getEncoding());
                byte[] newValueBytes = getPropertyAsBytes(newValue, getEncoding());

                if (originalValueBytes == null) {
                    originalValueBytes = new byte[0];
                } else {
                    originalValueBytes = maybeAppendEOL(originalValueBytes);
                }

                boolean newValueHadEol = newValueBytes != null && newValueBytes.length > 0 &&
                        (newValueBytes[newValueBytes.length - 1] == SVNProperty.EOL_CR_BYTES[0] ||
                        newValueBytes[newValueBytes.length - 1] == SVNProperty.EOL_LF_BYTES[0]);

                if (newValueBytes == null) {
                    newValueBytes = new byte[0];
                } else {
                    newValueBytes = maybeAppendEOL(newValueBytes);

                QDiffUniGenerator.setup();
                Map properties = new SVNHashMap();

                properties.put(QDiffGeneratorFactory.IGNORE_EOL_PROPERTY, Boolean.valueOf(getDiffOptions().isIgnoreEOLStyle()));
                properties.put(QDiffGeneratorFactory.HUNK_DELIMITER, "##");
                if (getDiffOptions().isIgnoreAllWhitespace()) {
                    properties.put(QDiffGeneratorFactory.IGNORE_SPACE_PROPERTY, QDiffGeneratorFactory.IGNORE_ALL_SPACE);
                } else if (getDiffOptions().isIgnoreAmountOfWhitespace()) {
                    properties.put(QDiffGeneratorFactory.IGNORE_SPACE_PROPERTY, QDiffGeneratorFactory.IGNORE_SPACE_CHANGE);
                }

                QDiffGenerator generator = new QDiffUniGenerator(properties, "");
                Writer writer = new OutputStreamWriter(outputStream, getEncoding());
                QDiffManager.generateTextDiff(new ByteArrayInputStream(originalValueBytes), new ByteArrayInputStream(newValueBytes),
                        getEncoding(), writer, generator);
                writer.flush();
                if (!newValueHadEol) {
                    displayString(outputStream, "\\ No newline at end of property");
    private byte[] maybeAppendEOL(byte[] buffer) {
        if (buffer.length == 0) {
            return buffer;
        }

        byte lastByte = buffer[buffer.length - 1];
        if (lastByte == SVNProperty.EOL_CR_BYTES[0]) {
            return buffer;
        } else if (lastByte != SVNProperty.EOL_LF_BYTES[0]) {
            final byte[] newBuffer = new byte[buffer.length + getEOL().length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            System.arraycopy(getEOL(), 0, newBuffer, buffer.length, getEOL().length);
            return newBuffer;
        } else {
            return buffer;
        }
    }

            displayString(outputStream, "diff --git ");
            displayFirstGitPath(outputStream, path1);
            displayString(outputStream, " ");
            displaySecondGitPath(outputStream, path2);
            displayString(outputStream, "diff --git ");
            displayFirstGitPath(outputStream, path1);
            displayString(outputStream, " ");
            displaySecondGitPath(outputStream, path2);
            displayString(outputStream, "diff --git ");
            displayFirstGitPath(outputStream, copyFromPath);
            displayString(outputStream, " ");
            displaySecondGitPath(outputStream, path2);
            displayString(outputStream, "diff --git ");
            displayFirstGitPath(outputStream, copyFromPath);
            displayString(outputStream, " ");
            displaySecondGitPath(outputStream, path2);
            displayString(outputStream, "diff --git ");
            displayFirstGitPath(outputStream, path1);
            displayString(outputStream, " ");
            displaySecondGitPath(outputStream, path2);
    private void displayFirstGitPath(OutputStream outputStream, String path1) throws IOException {
        displayGitPath(outputStream, path1, "a/", false);
    }

    private void displaySecondGitPath(OutputStream outputStream, String path2) throws IOException {
        displayGitPath(outputStream, path2, "b/", false);
    }

    private void displayFirstGitLabelPath(OutputStream outputStream, String path1, String revision1, SvnDiffCallback.OperationKind operation) throws IOException {
        String pathPrefix = "a/";
        if (operation == SvnDiffCallback.OperationKind.Added) {
            path1 = "/dev/null";
            pathPrefix = "";
        }
        displayGitPath(outputStream, getLabel(path1, revision1), pathPrefix, true);
    }

    private void displaySecondGitLabelPath(OutputStream outputStream, String path2, String revision2, SvnDiffCallback.OperationKind operation) throws IOException {
        String pathPrefix = "b/";
        if (operation == SvnDiffCallback.OperationKind.Deleted) {
            path2 = "/dev/null";
            pathPrefix = "";
        }
        displayGitPath(outputStream, getLabel(path2, revision2), pathPrefix, true);
    }

    private void displayGitPath(OutputStream outputStream, String path1, String pathPrefix, boolean label) throws IOException {
//        if (!label && path1.length() == 0) {
//            displayString(outputStream, ".");
//        } else {
            displayString(outputStream, pathPrefix);
            displayString(outputStream, path1);
//        }
    }

    public SVNDiffOptions getDiffOptions() {
        if (diffOptions == null) {
            diffOptions = new SVNDiffOptions();
        return diffOptions;
    }

    public void setExternalDiffCommand(String externalDiffCommand) {
        this.externalDiffCommand = externalDiffCommand;
    }

    public void setRawDiffOptions(List<String> rawDiffOptions) {
        this.rawDiffOptions = rawDiffOptions;
    }

    public void setDiffOptions(SVNDiffOptions diffOptions) {
        this.diffOptions = diffOptions;
    }

    public void setDiffDeleted(boolean diffDeleted) {
        this.diffDeleted = diffDeleted;
    }

    public void setBasePath(File absoluteFile) {
        setBaseTarget(SvnTarget.fromFile(absoluteFile));
    }

    public void setFallbackToAbsolutePath(boolean fallbackToAbsolutePath) {
        this.fallbackToAbsolutePath = fallbackToAbsolutePath;
    }

    public void setOptions(ISVNOptions options) {
        this.options = options;