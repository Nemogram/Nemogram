package org.nemogram.messenger.export;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.Vector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HtmlExportWriter implements ExportWriter {

    private static final int MESSAGES_PER_PAGE = 2500;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private static final String CSS =
            "body{font-family:-apple-system,sans-serif;background:#17212b;color:#fff;margin:0}" +
                    ".page_wrap{max-width:900px;margin:0 auto;padding:20px}" +
                    ".page_header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}" +
                    ".page_header h4{color:#fff;margin:0}" +
                    ".message{margin-bottom:8px}" +
                    ".message.default .body{background:#2b5278;border-radius:12px;padding:8px 12px;display:inline-block;max-width:80%}" +
                    ".message.service .body{color:#6c7883;font-size:12px;text-align:center;width:100%;display:block}" +
                    ".from_name{color:#5bbbf5;font-weight:600;font-size:13px;margin-bottom:2px}" +
                    ".text{font-size:14px;line-height:1.5;white-space:pre-wrap;word-break:break-word}" +
                    ".date{color:#6c7883;font-size:11px;margin-top:4px;text-align:right}" +
                    ".media_wrap img,.media_wrap video{max-width:100%;border-radius:8px;margin-top:4px;display:block}" +
                    ".pagination{display:flex;gap:8px;align-items:center;margin-top:24px;justify-content:center}" +
                    ".pagination a{color:#5bbbf5;text-decoration:none;padding:6px 14px;border:1px solid #2b5278;border-radius:8px;font-size:13px}" +
                    ".pagination a:hover{background:#2b5278}" +
                    ".pagination .current{color:#fff;padding:6px 14px;font-size:13px}" +
                    ".reactions{display:flex;flex-wrap:wrap;gap:4px;margin-top:6px}" +
                    ".reaction{background:rgba(91,187,245,0.15);border:1px solid rgba(91,187,245,0.3);" +
                    "border-radius:12px;padding:2px 8px;font-size:13px;display:inline-flex;" +
                    "align-items:center;gap:4px;color:#5bbbf5}" +
                    ".reaction .count{font-size:12px;color:#aaa}";

    private final File exportDir;
    private final File mediaDir;
    private final int account;

    private final Map<Long, String> customEmojiCache = new HashMap<>();

    private String chatTitle;
    private BufferedWriter writer;

    private int currentPage = 1;
    private int messagesOnPage = 0;
    private int totalMessages = 0;
    private int totalPages = 1;

    public HtmlExportWriter(File exportDir, int account) {
        this.exportDir = exportDir;
        this.account = account;
        this.mediaDir = new File(exportDir, "media");
        this.mediaDir.mkdirs();
    }

    @Override
    public void begin(String chatTitle) throws IOException {
        this.chatTitle = chatTitle;
        openPage(1, false);
    }

    @Override
    public void writeMessage(MessageObject msg, File mediaFile) throws IOException {
        if (messagesOnPage >= MESSAGES_PER_PAGE) {
            closePage(true);
            currentPage++;
            messagesOnPage = 0;
            openPage(currentPage, true);
        }

        if (msg.messageOwner.action != null) {
            writer.write("<div class='message service'><div class='body'>— " + esc(getActionText(msg)) + " —</div></div>\n");
        } else {
            String from = getSenderName(msg);
            String date = SDF.format(new Date((long) msg.messageOwner.date * 1000));
            String text = msg.messageOwner.message != null ? msg.messageOwner.message : "";

            writer.write("<div class='message default'><div class='body'>");
            writer.write("<div class='from_name'>" + esc(from) + "</div>");

            if (mediaFile != null && mediaFile.exists()) {
                File dest = new File(mediaDir, mediaFile.getName());
                copyFile(mediaFile, dest);
                String rel = "media/" + escAttr(dest.getName());
                if (msg.isPhoto()) {
                    writer.write("<div class='media_wrap'><img src='" + rel + "'/></div>");
                } else if (msg.isVideo()) {
                    writer.write("<div class='media_wrap'><video src='" + rel + "' controls></video></div>");
                } else {
                    writer.write("<div class='media_wrap'><a href='" + rel + "'>" + esc(dest.getName()) + "</a></div>");
                }
            }

            if (!text.isEmpty()) {
                writer.write("<div class='text'>" + esc(text) + "</div>");
            }

            writer.write("<div class='date'>" + date + "</div>");
            writer.write(buildReactionsHtml(msg));
            writer.write("</div></div>\n");
        }

        messagesOnPage++;
        totalMessages++;
    }

    @Override
    public File end() throws IOException {
        totalPages = currentPage;
        closePage(false);
        return pageFile(1);
    }

    private void openPage(int pageNum, boolean hasPrev) throws IOException {
        writer = new BufferedWriter(new FileWriter(pageFile(pageNum)), 32 * 1024);
        writer.write("<!DOCTYPE html><html><head><meta charset='utf-8'>");
        writer.write("<title>" + esc(chatTitle) + "</title>");
        writer.write("<style>" + CSS + "</style></head><body><div class='page_wrap'>");
        writer.write("<div class='page_header'>");
        writer.write("<h4>" + esc(chatTitle) + "</h4>");
        writer.write(buildPagination(pageNum, hasPrev, false));
        writer.write("</div>");
        writer.write("<div class='history'>\n");
    }

    private void closePage(boolean hasNext) throws IOException {
        writer.write("</div>");
        writer.write(buildPagination(currentPage, currentPage > 1, hasNext));
        writer.write("</div></body></html>");
        writer.flush();
        writer.close();
        writer = null;
    }

    private String buildPagination(int pageNum, boolean hasPrev, boolean hasNext) {
        if (!hasPrev && !hasNext) return "";

        StringBuilder sb = new StringBuilder("<div class='pagination'>");
        if (hasPrev) {
            sb.append("<a href='").append(escAttr(pageFileName(pageNum - 1))).append("'>&larr; Prev</a>");
        }
        sb.append("<span class='current'>Page ").append(pageNum).append("</span>");
        if (hasNext) {
            sb.append("<a href='").append(escAttr(pageFileName(pageNum + 1))).append("'>Next &rarr;</a>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private File pageFile(int pageNum) {
        return new File(exportDir, pageFileName(pageNum));
    }

    private String pageFileName(int pageNum) {
        return pageNum == 1 ? "messages.html" : "messages_" + pageNum + ".html";
    }

    private String getSenderName(MessageObject msg) {
        return ChatExportManager.getSenderName(msg);
    }

    private String getActionText(MessageObject msg) {
        if (msg.messageOwner.action instanceof TLRPC.TL_messageActionChatAddUser)
            return "User joined";
        if (msg.messageOwner.action instanceof TLRPC.TL_messageActionChatDeleteUser)
            return "User left";
        if (msg.messageOwner.action instanceof TLRPC.TL_messageActionChatCreate)
            return "Chat created";
        if (msg.messageOwner.action instanceof TLRPC.TL_messageActionPinMessage)
            return "Message pinned";
        return "Service message";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;");
    }

    private String buildReactionsHtml(MessageObject msg) {
        TLRPC.TL_messageReactions reactions = msg.messageOwner.reactions;
        if (reactions == null || reactions.results == null || reactions.results.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("<div class='reactions'>");
        boolean any = false;
        for (TLRPC.ReactionCount rc : reactions.results) {
            if (rc.reaction == null) continue;
            String emoji;
            if (rc.reaction instanceof TLRPC.TL_reactionEmoji) {
                emoji = esc(((TLRPC.TL_reactionEmoji) rc.reaction).emoticon);
            } else if (rc.reaction instanceof TLRPC.TL_reactionCustomEmoji) {
                long docId = ((TLRPC.TL_reactionCustomEmoji) rc.reaction).document_id;
                emoji = customEmojiCache.getOrDefault(docId, "✦");
            } else {
                continue;
            }
            sb.append("<span class='reaction'>")
                    .append(emoji)
                    .append("<span class='count'>").append(rc.count).append("</span>")
                    .append("</span>");
            any = true;
        }
        sb.append("</div>");
        return any ? sb.toString() : "";
    }

    public void resolveCustomEmoji(Set<Long> docIds) {
        List<Long> toFetch = docIds.stream()
                .filter(id -> !customEmojiCache.containsKey(id))
                .collect(Collectors.toList());
        if (toFetch.isEmpty()) return;

        CountDownLatch latch = new CountDownLatch(1);
        AndroidUtilities.runOnUIThread(() -> {
            TLRPC.TL_messages_getCustomEmojiDocuments req =
                    new TLRPC.TL_messages_getCustomEmojiDocuments();
            req.document_id = new ArrayList<>(toFetch);

            ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> {
                if (response instanceof Vector) {
                    for (Object obj : ((Vector) response).objects) {
                        if (!(obj instanceof TLRPC.Document)) continue;
                        TLRPC.Document doc = (TLRPC.Document) obj;
                        for (TLRPC.DocumentAttribute attr : doc.attributes) {
                            if (attr instanceof TLRPC.TL_documentAttributeCustomEmoji) {
                                String alt = ((TLRPC.TL_documentAttributeCustomEmoji) attr).alt;
                                if (alt != null && !alt.isEmpty()) {
                                    customEmojiCache.put(doc.id, alt);
                                }
                            }
                        }
                    }
                }
                latch.countDown();
            });
        });

        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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