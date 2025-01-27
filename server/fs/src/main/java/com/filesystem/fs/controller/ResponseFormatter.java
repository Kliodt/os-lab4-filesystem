package com.filesystem.fs.controller;

public final class ResponseFormatter {
    static String format(boolean isOk, String errText, String textContent, String rawContent) {
        StringBuilder sb = new StringBuilder();
        if (!isOk) {
            sb.append("0;-1;");
            if (errText != null) {
                sb.append("1;");
                sb.append(errText);
            } else {
                sb.append("1;error;");
            }
        } else {
            sb.append("0;1;1;ok;");
            if (textContent != null) {
                sb.append("2;");
                sb.append(textContent); // todo: encode base 64
                sb.append(";");
            }
            if (rawContent != null) {
                sb.append("3;");
                sb.append(rawContent);
                sb.append(";");
            }
        }
        return sb.toString();
    }
}
