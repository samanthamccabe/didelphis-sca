package org.haedus.soundchange;

/**
 * Class NormalizationMode
 *
 * @since 07/30/2014
 */
public enum NormalizerMode {
    NFD("NFD"),
    NFC("NFC"),
    NFKD("NFKD"),
    NFKC("NFKC"),
    NONE("NONE");

    private String value;

    NormalizerMode(String value) {
        this.value = value;
    }
}
