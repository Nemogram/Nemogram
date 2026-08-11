package org.nemogram.messenger.helpers.transcribe;

public class OfflineTranscribeProvider {
    public final String packageName;
    public final String serviceClassName;
    public final CharSequence label;

    public OfflineTranscribeProvider(String packageName, String serviceClassName, CharSequence label) {
        this.packageName = packageName;
        this.serviceClassName = serviceClassName;
        this.label = label;
    }

    public String id() {
        return packageName + "/" + serviceClassName;
    }
}
