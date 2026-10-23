package org.nemogram.messenger.helpers.remote;

import org.telegram.tgnet.TLRPC;

import java.util.Arrays;
import java.util.List;

public class ConfigHelper {

    private static final List<Long> CAT_VERIFIED_CHAT_IDS = Arrays.asList(
            1349472891L,
            1339737452L,
            1302242053L,
            1715773134L
    );

    public static boolean isChatCat(TLRPC.Chat chat) {
        return CAT_VERIFIED_CHAT_IDS.stream().anyMatch(id -> id == chat.id || id == -2000000000000L - chat.id);
    }
}
