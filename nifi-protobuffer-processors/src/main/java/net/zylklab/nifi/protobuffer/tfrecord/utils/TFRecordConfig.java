package net.zylklab.nifi.protobuffer.tfrecord.utils;

public class TFRecordConfig {
    private Boolean enableDictionaryEncoding;
    private Boolean enableValidation;
    
    public Boolean getEnableDictionaryEncoding() {
        return enableDictionaryEncoding;
    }

    public void setEnableDictionaryEncoding(Boolean enableDictionaryEncoding) {
        this.enableDictionaryEncoding = enableDictionaryEncoding;
    }

    public Boolean getEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(Boolean enableValidation) {
        this.enableValidation = enableValidation;
    }
}
