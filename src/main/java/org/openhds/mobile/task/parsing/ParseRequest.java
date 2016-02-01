package org.openhds.mobile.task.parsing;

import com.github.batkinson.jrsync.zsync.IOUtil;

import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.parsing.entities.EntityParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream, EntityParser, and Gateway for parsing entities.
 * <p/>
 * Declare everything needed to parse an input stream into OpenHDS entities:
 * - the input stream to read
 * - an entity parser to parse incoming data into entity objects
 * - a gateway to persist entities in the database
 * <p/>
 * Pass a ParseRequest to a ParseTask to make it go.
 * <p/>
 * BSH
 */
public class ParseRequest<T> {

    private final int titleId;
    private ParserInputStream inputStream;
    private final EntityParser<T> entityParser;
    private final Gateway<T> gateway;

    public ParseRequest(int titleId, EntityParser<T> entityParser, Gateway<T> gateway) {
        this.titleId = titleId;
        this.entityParser = entityParser;
        this.gateway = gateway;
    }

    public int getTitleId() {
        return titleId;
    }

    public ParserInputStream getInputStream() {
        return inputStream;
    }

    public void setInputFile(File inputFile) throws FileNotFoundException {
        this.inputStream = new ParserInputStream(inputFile);
    }

    public void setInputStream(InputStream inputStream, long length) {
        this.inputStream = new ParserInputStream(inputStream, length);
    }

    public void setInputStream(InputStream inputStream) {
        setInputStream(inputStream, 0);
    }

    public void clearInput() {
        this.inputStream = null;
    }

    public EntityParser<T> getEntityParser() {
        return entityParser;
    }

    public Gateway<T> getGateway() {
        return gateway;
    }
}

class ParserInputStream extends InputStream {

    InputStream stream;
    long length;
    long bytesRead;

    ParserInputStream(File f) throws FileNotFoundException {
        this(new BufferedInputStream(new FileInputStream(f)), f.length());
    }

    ParserInputStream(InputStream stream, long length) {
        this.stream = stream;
        this.length = length;
    }

    @Override
    public int read() throws IOException {
        int read = stream.read();
        if (read >= 0)
            bytesRead++;
        return read;
    }

    public int getPercentRead() {
        return (int) ((length == 0 ? 1 : (double)bytesRead / length) * 100);
    }

    @Override
    public void close() throws IOException {
        IOUtil.close(stream);
    }
}

