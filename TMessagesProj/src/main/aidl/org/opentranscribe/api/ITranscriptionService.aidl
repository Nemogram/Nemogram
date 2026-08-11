package org.opentranscribe.api;

import android.os.ParcelFileDescriptor;
import org.opentranscribe.api.TranscriptionRequest;
import org.opentranscribe.api.TranscriberCapabilities;
import org.opentranscribe.api.ITranscriptionCallback;
import org.opentranscribe.api.ITranscriptionSession;

interface ITranscriptionService {
    TranscriberCapabilities getCapabilities();

    ITranscriptionSession transcribe(
        in ParcelFileDescriptor audio,
        in TranscriptionRequest request,
        ITranscriptionCallback callback);
}
