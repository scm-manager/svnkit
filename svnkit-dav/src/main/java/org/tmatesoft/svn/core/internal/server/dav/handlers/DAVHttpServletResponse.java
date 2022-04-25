package org.tmatesoft.svn.core.internal.server.dav.handlers;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Extends the {@link HttpServletResponse} with methods for the svn dav protocol.
 *
 * @author Sebastian Sdorra
 */
public class DAVHttpServletResponse extends HttpServletResponseWrapper {

    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    private Writer throwingWriter;

    private DAVHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    /**
     * Wrap the given {@link HttpServletResponse} or cast it to a {@link DAVHttpServletResponse}.
     *
     * @param response http servlet response
     * @return dav servlet response
     */
    public static DAVHttpServletResponse wrapOrCast(HttpServletResponse response) {
        if (response instanceof DAVHttpServletResponse) {
            return (DAVHttpServletResponse) response;
        }
        return new DAVHttpServletResponse(response);
    }

    /**
     * Returns a writer which is able to throw an {@link IOException} on write. This method should be used instead of
     * {@link #getWriter()}, which returns an exception swallowing {@link java.io.PrintWriter}. If a
     * {@link java.io.PrintWriter} is used an client abort has no impact on the running action. If the throwing
     * {@link Writer} is used an client abort stops the current running action immediately.
     *
     * @return {@link Writer} which is able to throw an {@link IOException} on a client connection abort
     *
     * @throws IOException
     */
    public Writer getThrowingWriter() throws IOException {
        String characterEncoding = getCharacterEncodingOrDefault();
        if (throwingWriter == null) {
            throwingWriter = new OutputStreamWriter(getOutputStream(), characterEncoding);
        }
        return throwingWriter;
    }

    private String getCharacterEncodingOrDefault() {
        String characterEncoding = getCharacterEncoding();
        if (isNullOrEmpty(characterEncoding)) {
            characterEncoding = DEFAULT_CHARACTER_ENCODING;
        }
        return characterEncoding;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    @Override
    public void flushBuffer() throws IOException {
        closeThrowingWriter();
        super.flushBuffer();
    }

    private void closeThrowingWriter() throws IOException {
        if (throwingWriter != null) {
            throwingWriter.close();
            throwingWriter = null;
        }
    }

}
