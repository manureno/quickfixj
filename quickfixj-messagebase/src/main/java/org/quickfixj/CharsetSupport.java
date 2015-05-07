/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package org.quickfixj;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class CharsetSupport {

    private static final Charset CHARSET_ISO_8859_1 = Charset.forName("ISO-8859-1");
    private static final Charset CHARSET_ASCII = Charset.forName("US-ASCII");

    private static String charset = getDefaultCharset();
    private static Charset charsetInstance = Charset.forName(charset);

    public static String getDefaultCharset() {
        return "ISO-8859-1";
    }

    /**
     * Returns whether the given charset's byte representation of a string
     * is equivalent (as unsigned values) to the string characters themselves.
     *
     * @param charset a charset
     * @return whether the charset encoding is string-equivalent
     */
    public static boolean isStringEquivalent(Charset charset) {
        // ASCII and ISO-8859-1 are unique in that their encoded byte representation
        // is equivalent to their respective Java String (UTF-16BE) chars
        return charset.equals(CHARSET_ISO_8859_1) || charset.equals(CHARSET_ASCII);
    }

    public static void setCharset(String charset) throws UnsupportedEncodingException {
        CharsetSupport.charset = validate(charset);
        CharsetSupport.charsetInstance = Charset.forName(charset);
    }

    public static String getCharset() {
        return charset;
    }

    public static Charset getCharsetInstance() {
        return charsetInstance;
    }

    public static String validate(String charset) throws UnsupportedEncodingException {
        if (!Charset.isSupported(charset)) {
            throw new UnsupportedEncodingException(charset);
        }
        return charset;
    }
    
    public static int checksum(Charset charset, String data, boolean isEntireMessage) {
        int sum = 0;
        if (CharsetSupport.isStringEquivalent(charset)) { // optimization - skip encoding
            int end = isEntireMessage ? data.lastIndexOf("\00110=") : -1;
            int len = end > -1 ? end + 1 : data.length();
            for (int i = 0; i < len; i++) {
                sum += data.charAt(i);
            }
        } else {
            byte[] bytes = data.getBytes(charset);
            int len = bytes.length;
            if (isEntireMessage && bytes[len - 8] == '\001' && bytes[len - 7] == '1'
                    && bytes[len - 6] == '0' && bytes[len - 5] == '=')
                len = len - 7;
            for (int i = 0; i < len; i++) {
                sum += (bytes[i] & 0xFF);
            }
        }
        return sum & 0xFF; // better than sum % 256 since it avoids overflow issues
    }
    
    /**
     * Calculates the checksum for the given message
     * (excluding existing checksum field, if one exists).
     * The {@link CharsetSupport#setCharset global charset} is used.
     *
     * @param message the message to calculate the checksum on
     * @return the calculated checksum
     */
    public static int checksum(String message) {
        return CharsetSupport.checksum(CharsetSupport.getCharsetInstance(), message, true);
    }
    
    /**
     * Calculates the length of the byte representation
     * of the given string in the given charset.
     *
     * @param charset the charset used in encoding the data
     * @param data the data to calculate the length on
     * @return the calculated length
     */
    public static int length(Charset charset, String data) {
        return CharsetSupport.isStringEquivalent(charset) ? data.length() : data.getBytes(charset).length;
    }
}
