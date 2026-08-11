package org.nemogram.messenger.helpers.transcribe;

public interface OfflineTranscribeSession {
    void cancel();

    OfflineTranscribeSession NOOP = () -> {
    };
}
