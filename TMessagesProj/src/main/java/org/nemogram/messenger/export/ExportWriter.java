package org.nemogram.messenger.export;

import org.telegram.messenger.MessageObject;

import java.io.File;
import java.io.IOException;

public interface ExportWriter {
    void begin(String chatTitle) throws IOException;

    void writeMessage(MessageObject msg, File mediaFile) throws IOException;

    File end() throws IOException;
}