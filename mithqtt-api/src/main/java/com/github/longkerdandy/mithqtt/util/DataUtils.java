package com.github.longkerdandy.mithqtt.util;

public class DataUtils {

    /**
     * byte[] to hex string
     *
     * @param src byte[]
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v).toUpperCase();
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public static String shortToHexString(Short val) {
        return Integer.toHexString(val.intValue());
    }
}
