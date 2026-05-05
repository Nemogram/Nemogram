package org.nemogram.messenger.export;

import org.telegram.messenger.MessageObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonExportWriter implements ExportWriter {

    private final File exportDir;
    private final File mediaDir;
    private BufferedWriter writer;
    private boolean firstMessage = true;

    public JsonExportWriter(File exportDir) {
        this.exportDir = exportDir;
        this.mediaDir = new File(exportDir, "media");
        this.mediaDir.mkdirs();
    }

    @Override
    public void begin(String chatTitle) throws IOException {
        writer = new BufferedWriter(new FileWriter(new File(exportDir, "messages.json")));
        writer.write("{\"name\":" + jsonStr(chatTitle) + ",\"messages\":[\n");
    }

    @Override
    public void writeMessage(MessageObject msg, File mediaFile) throws IOException {
        if (!firstMessage) writer.write(",\n");
        firstMessage = false;

        String sender = ChatExportManager.getSenderName(msg);
        String text = msg.messageOwner.message != null ? msg.messageOwner.message : "";
        String mediaRel = null;

        if (mediaFile != null && mediaFile.exists()) {
            File dest = new File(mediaDir, mediaFile.getName());
            copyFile(mediaFile, dest);
            mediaRel = "media/" + dest.getName();
        }

        writer.write("{");
        writer.write("\"id\":" + msg.getId() + ",");
        writer.write("\"date\":" + msg.messageOwner.date + ",");
        writer.write("\"from\":" + jsonStr(sender) + ",");
        writer.write("\"text\":" + jsonStr(text));
        if (mediaRel != null) {
            writer.write(",\"media\":" + jsonStr(mediaRel));
        }
        writer.write("}");
    }

    @Override
    public File end() throws IOException {
        writer.write("\n]}");
        writer.flush();
        writer.close();
        return new File(exportDir, "messages.json");
    }

    private String jsonStr(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private void copyFile(File src, File dst) throws IOException {
        if (dst.exists()) return;
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
    }
}