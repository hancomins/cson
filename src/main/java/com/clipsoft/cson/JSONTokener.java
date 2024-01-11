
package com.clipsoft.cson;


import com.clipsoft.cson.util.NoSynchronizedBufferReader;
import com.clipsoft.cson.util.NoSynchronizedStringReader;
import com.clipsoft.cson.util.NumberConversionUtil;

import java.io.*;

/*
JSON.org 라이브러리를 가져와 수정하여 사용하였다.
https://github.com/stleary/JSON-java/blob/master/src/main/java/org/json/JSONTokener.java
 */

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * @author JSON.org
 * @version 2014-05-03
 */
@SuppressWarnings("UnusedReturnValue")
class JSONTokener {
    /** current read character position on the current line. */
    private long character;
    /** flag to indicate if the end of the input has been found. */
    private boolean eof;
    /** current read index of the input. */
    private long index;
    /** current line of the input. */
    private long line;
    /** previous character read from the input. */
    private char previous;
    /** Reader for the input. */
    private final Reader reader;
    /** flag to indicate that a previous character was requested. */
    private boolean usePrevious;
    /** the number of characters read in the previous line. */
    private long characterPreviousLine;

    private final JSONOptions jsonOption;


    JSONTokener(Reader reader, JSONOptions jsonOptions) {
        this.reader = reader.markSupported()
                ? reader
                : new NoSynchronizedBufferReader(reader);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.characterPreviousLine = 0;
        this.line = 1;
        this.jsonOption = jsonOptions;
    }

    JSONOptions getJsonOption() {
        return jsonOption;
    }




    JSONTokener(String s,JSONOptions jsonOptions) {
        this(new NoSynchronizedStringReader(s), jsonOptions);
    }



    void back() throws CSONException {
        if (this.usePrevious || this.index <= 0) {
            throw new CSONException("Stepping back two steps is not supported");
        }
        this.decrementIndexes();
        this.usePrevious = true;
        this.eof = false;
    }

    @SuppressWarnings("unused")
    boolean canGoBack() {
        return !this.usePrevious && this.index > 0;
    }

    private void decrementIndexes() {
        this.index--;
        if(this.previous=='\r' || this.previous == '\n') {
            this.line--;
            this.character=this.characterPreviousLine ;
        } else if(this.character > 0){
            this.character--;
        }
    }

    @SuppressWarnings("unused")
    static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }

    /**
     * Checks if the end of the input has been reached.
     *
     * @return true if at the end of the file and we didn't step back
     */
    boolean end() {
        return this.eof && !this.usePrevious;
    }


    @SuppressWarnings("unused")
    boolean more() throws CSONException {
        if(this.usePrevious) {
            return true;
        }
        try {
            this.reader.mark(1);
        } catch (IOException e) {
            throw new CSONException("Unable to preserve stream position", e);
        }
        try {
            // -1 is EOF, but next() can not consume the null character '\0'
            if(this.reader.read() <= 0) {
                this.eof = true;
                return false;
            }
            this.reader.reset();
        } catch (IOException e) {
            throw new CSONException("Unable to read the next character from the stream", e);
        }
        return true;
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws CSONException Thrown if there is an error reading the source string.
     */
    char next() throws CSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException exception) {
                throw new CSONException(exception);
            }
        }
        if (c <= 0) { // End of stream
            this.eof = true;
            return 0;
        }
        this.incrementIndexes(c);
        this.previous = (char) c;
        return this.previous;
    }

    /**
     * Get the last character read from the input or '\0' if nothing has been read yet.
     * @return the last character read from the input.
     */
    protected char getPrevious() { return this.previous;}

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     * @param c the current character read.
     */
    private void incrementIndexes(int c) {
        if(c > 0) {
            this.index++;
            if(c=='\r') {
                this.line++;
                this.characterPreviousLine = this.character;
                this.character=0;
            }else if (c=='\n') {
                if(this.previous != '\r') {
                    this.line++;
                    this.characterPreviousLine = this.character;
                }
                this.character=0;
            } else {
                this.character++;
            }
        }
    }


    @SuppressWarnings("unused")
    char next(char c) throws CSONException {
        char n = this.next();
        if (n != c) {
            if(n > 0) {
                throw this.syntaxError("Expected '" + c + "' and instead saw '" +
                        n + "'");
            }
            throw this.syntaxError("Expected '" + c + "' and instead saw ''");
        }
        return n;
    }


    @SuppressWarnings("SameParameterValue")
    String next(int n) throws CSONException {
        if (n == 0) {
            return "";
        }

        char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = this.next();
            if (this.end()) {
                throw this.syntaxError("Substring bounds error");
            }
            pos += 1;
        }
        return new String(chars);
    }


    /**
     * Get the next char in the string, skipping whitespace.
     * @throws CSONException Thrown if there is an error reading the source string.
     * @return  A character, or 0 if there are no more characters.
     */
    char nextClean() throws CSONException {
        for (;;) {
            char c = this.next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    String nextString(char quote) throws CSONException {
        char c;
        StringBuilder sb = new StringBuilder();


        for (;;) {
            c = this.next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw this.syntaxError("Unterminated string");
                case '\\':
                    c = this.next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            try {
                                sb.append((char)Integer.parseInt(this.next(4), 16));
                            } catch (NumberFormatException e) {
                                throw this.syntaxError("Illegal escape.", e);
                            }
                            break;
                        case '\r' :
                            if(quote == '"') {
                                c = this.next();
                                if(c != '\n') {
                                    throw this.syntaxError("Illegal escape.");
                                }
                                break;
                            }
                        case '\n' :
                            if(quote == '"') {
                                break;
                            }
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw this.syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }


    @SuppressWarnings("SameParameterValue")
    String nextTo(char delimiter) throws CSONException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            char c = this.next();
            if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    this.back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    @SuppressWarnings("unused")
    String nextTo(String delimiters) throws CSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = this.next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    this.back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }

    @SuppressWarnings("SameParameterValue")
    String nextToFromString(String strDelimiters, boolean eofToError) throws CSONException {
        char c;
        char[] delimiters = strDelimiters.toCharArray();
        StringBuilder sb = new StringBuilder();
        int equalCount = 0;
        int delimiterEndIndex = delimiters.length - 1;
        for (;;) {
            c = this.next();
            if(c == 0) {
                if(eofToError) {
                    throw this.syntaxError("Unterminated string");
                }
                return sb.toString().trim();
            }
            else if (delimiters[equalCount] == c) {
                if(equalCount == delimiterEndIndex) {
                    return sb.substring(0,sb.length() - delimiterEndIndex).trim();
                }
                equalCount++;
            }
            else if(equalCount > 0 && delimiters[0] == c) {
                equalCount = 1;
            }
            else {
                equalCount = 0;
            }
            sb.append(c);
        }
    }


    @SuppressWarnings("SameParameterValue")
    void skipTo(String strDelimiters) throws CSONException {
        char c;
        char[] delimiters = strDelimiters.toCharArray();
        int equalCount = 0;
        int delimiterEndIndex = delimiters.length - 1;
        for (;;) {
            c = this.next();
            if(c == 0) {
                return;
            }
            else if (delimiters[equalCount] == c) {
                if(equalCount == delimiterEndIndex) {
                    return;
                }
                equalCount++;
            }
            else if(equalCount > 0 && delimiters[0] == c) {
                equalCount = 1;
            }
            else {
                equalCount = 0;
            }
        }
    }




    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws CSONException If syntax error.
     *
     * @return An object.
     */
    Object nextValue() throws CSONException {
        char c = this.nextClean();
        String string;
        boolean isEscapeMode = false;


        switch (c) {
            case '"':
                return this.nextString(c);
            case '\'':
                if(jsonOption.isAllowCharacter()) {
                    String value = this.nextString(c);
                    return this.stringToCharValue(value);

                }
                if(jsonOption.isAllowSingleQuotes()) {
                    return this.nextString(c);
                } else {
                    throw this.syntaxError("Unexpected single quote");
                }
            case '{':
                this.back();
                try {
                    return new CSONObject(this);
                } catch (StackOverflowError e) {
                    throw new CSONException("JSON Array or Object depth too large to process.", e);
                }
            case '[':
                this.back();
                try {
                    return new CSONArray(this);
                } catch (StackOverflowError e) {
                    throw new CSONException("JSON Array or Object depth too large to process.", e);
                }
        }

        StringBuilder sb = new StringBuilder();
        while (c >= ' ') {
            if(c == '\\' && !isEscapeMode) {
                isEscapeMode = true;
                c = this.next();
                continue;
            } else if(!isEscapeMode && ",:]}/\"[{;=#".indexOf(c) > -1) {
                break;
            }
            else {
                isEscapeMode = false;
            }
            sb.append(c);
            c = this.next();
        }

        if (!this.eof) {
            this.back();
        }

        string = sb.toString().trim();
        //noinspection StringEqualsEmptyString
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }


        Object value = stringToValue(string);
        if(!jsonOption.isAllowUnquoted() && value instanceof String) {
            throw this.syntaxError("Unquoted string");
        }
        return value;
    }


    @SuppressWarnings("unused")
    Object nextPureKey(JSONOptions... options) throws CSONException {
        char c = this.nextClean();
        String string;
        if (c == '"') {
            return this.nextString(c);
        }

        if (!this.eof) {
            this.back();
        }


        throw this.syntaxError("Missing value");

    }


    Object stringToCharValue(String string) {

        int length = string.length();

        //noinspection StringEqualsEmptyString
        if ("".equals(string)) {
            return 0;
        }

        if (length == 1) {
            return string.charAt(0);
        }

        if(jsonOption.isAllowHexadecimal()) {
            char initial = string.charAt(0);
            Exception err = null;
            if (length > 2 && initial == '0' && (string.charAt(1) == 'x' || string.charAt(1) == 'X')) {
                try {
                    return (char) Integer.parseInt(string.substring(2), 16);
                } catch (NumberFormatException e) {
                    err = e;
                }
            }
            else if(length == 5 && (initial == 'u' || initial == 'U')) {
                try {
                    return (char) Integer.parseInt(string.substring(1), 16);
                } catch (NumberFormatException e) {
                    err = e;
                }
            }  else if(length == 6 && (initial == 'u' || initial == 'U') && string.charAt(1) == '+') {
                try {
                    return (char) Integer.parseInt(string.substring(2), 16);
                } catch (NumberFormatException e) {
                    err = e;
                }
            }
            if(!jsonOption.isIgnoreNonNumeric()) {
                if(err != null) {
                    throw syntaxError("Invalid char value: " + string, err);
                }
            }
        }

        if(!jsonOption.isAllowSingleQuotes() && !jsonOption.isAllowUnquoted()) {
            throw  this.syntaxError("Invalid char value: " + string);
        }
        return string;

    }


    Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return null;
        }
        try {
            Number number = NumberConversionUtil.stringToNumber(string, jsonOption);
            if(number == null) {
                return string;
            } else {
                return number;
            }
        } catch (NumberFormatException e) {
            if(!jsonOption.isIgnoreNonNumeric()) {
                throw this.syntaxError("Invalid number format: " + string, e);
            }
        }
        return string;

    }

    /*Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }

        // check JSON key words true/false/null
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return null;
        }

        if (jsonOption.isAllowNaN() && "NaN".equalsIgnoreCase(string)) {
            return Double.NaN;
        }

        if(jsonOption.isAllowInfinity()) {
            if ("Infinity".equalsIgnoreCase(string) || (jsonOption.isAllowPositiveSing() && "+Infinity".equalsIgnoreCase(string))) {
                return Double.POSITIVE_INFINITY;
            }
            if ("-Infinity".equalsIgnoreCase(string)) {
                return Double.NEGATIVE_INFINITY;
            }
        }


        char initial = string.charAt(0);
        int length = string.length();
        String originalString = null;
        if(jsonOption.isAllowHexadecimal() && initial == '0' && length > 2) {
            char second = string.charAt(1);
            if(second == 'x' || second == 'X') {
                try {
                    return Integer.parseInt(string.substring(2), 16);
                } catch (NumberFormatException e) {
                    if(!jsonOption.isIgnoreNumberFormatError()) {
                        throw this.syntaxError("Invalid number format: " + string, e);
                    }
                }
            }
        }



        if(jsonOption.isLeadingZeroOmission() && initial == '.' && length > 1 ) {
            originalString = string;
            string = "0" + string;
            initial = '0';
        }

        if ((initial >= '0' && initial <= '9') || initial == '-' || (jsonOption.isAllowPositiveSing() && initial == '+')) {
            if(jsonOption.isLeadingZeroOmission() && string.charAt(length - 1) == '.') {
                originalString = string;
                string = string + "0";
            } if(initial == '+') {
                originalString = string;
                string = string.substring(1);
            }

            try {
                return stringToNumber(string);
            } catch (NumberFormatException e) {
                if(!jsonOption.isIgnoreNumberFormatError()) {
                    throw this.syntaxError("Invalid number format: " + string, e);
                }
                if(originalString != null) {
                    return originalString;
                }
            }
        }
        return string;
    }*/

    /*protected static Number stringToNumber(final String val) throws NumberFormatException {


        char initial = val.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            // decimal representation
            if (isDecimalNotation(val)) {
                // Use a BigDecimal all the time so we keep the original
                // representation. BigDecimal doesn't support -0.0, ensure we
                // keep that by forcing a decimal.
                try {
                    BigDecimal bd = new BigDecimal(val);
                    if(initial == '-' && BigDecimal.ZERO.compareTo(bd)==0) {
                        //noinspection UnnecessaryBoxing
                        return Double.valueOf(-0.0);
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    // this is to support "Hex Floats" like this: 0x1.0P-1074
                    try {
                        Double d = Double.valueOf(val);
                        if(d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val ["+val+"] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val ["+val+"] is not a valid number.");
                    }
                }
            }
            // block items like 00 01 etc. Java number parsers treat these as Octal.
            if(initial == '0' && val.length() > 1) {
                char at1 = val.charAt(1);
                if(at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                char at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if(at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            }

            BigInteger bi = new BigInteger(val);
            if(bi.bitLength() <= 31){
                return bi.intValue();
            }
            if(bi.bitLength() <= 63){
                return bi.longValue();
            }
            return bi;
        }
        throw new NumberFormatException("val ["+val+"] is not a valid number.");
    }

    protected static boolean isDecimalNotation(final String val) {
        return val.indexOf('.') > -1 || val.indexOf('e') > -1
                || val.indexOf('E') > -1 || "-0".equals(val);
    }*/


    @SuppressWarnings("SameParameterValue")
    char skipTo(char to) throws CSONException {
        char c;
        try {
            long startIndex = this.index;
            long startCharacter = this.character;
            long startLine = this.line;
            this.reader.mark(1000000);
            do {
                c = this.next();
                if (c == 0) {
                    // in some readers, reset() may throw an exception if
                    // the remaining portion of the input is greater than
                    // the mark size (1,000,000 above).
                    this.reader.reset();
                    this.index = startIndex;
                    this.character = startCharacter;
                    this.line = startLine;
                    return 0;
                }
            } while (c != to);
            this.reader.mark(1);
        } catch (IOException exception) {
            throw new CSONException(exception);
        }
        this.back();
        return c;
    }

    /**
     * Make a CSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A CSONException object, suitable for throwing
     */
    CSONException syntaxError(String message) {
        return new CSONException(message + this.toString());
    }

    /**
     * Make a CSONException to signal a syntax error.
     *
     * @param message The error message.
     * @param causedBy The throwable that caused the error.
     * @return  A CSONException object, suitable for throwing
     */
    CSONException syntaxError(String message, Throwable causedBy) {
        return new CSONException(message + this.toString(), causedBy);
    }

    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return " at " + this.index + " [character " + this.character + " line " +
                this.line + "]";
    }
}
