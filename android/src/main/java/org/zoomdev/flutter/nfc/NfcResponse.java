package org.zoomdev.flutter.nfc;


import java.util.Arrays;

public class NfcResponse {
    public static final short VALID_VALUE = 144;
    public static final NfcResponse OK = new NfcResponse(new byte[]{-112, 0});
    private byte[] src;

    public NfcResponse(byte[] src) {
        this.src = src;
    }

    public byte[] getData() {
        return Arrays.copyOf(this.src, this.src.length - 2);
    }

    public String getStr() {
        return HexUtil.encodeHexStr(this.src, this.src.length - 2);
    }

    public boolean isOk() {
        return this.getSw() == 144;
    }

    public short getSw() {
        return HexUtil.toShort(this.src, this.src.length - 2);
    }

    public NfcResponse validate() throws NfcException {
        short sw = this.getSw();
        if (sw != 144) {
            throw new NfcException(sw);
        } else {
            return this;
        }
    }

    public byte[] getRowData() {
        return this.src;
    }
}
