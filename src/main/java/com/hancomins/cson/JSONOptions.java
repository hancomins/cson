package com.hancomins.cson;


@SuppressWarnings("UnusedReturnValue")
public class JSONOptions implements StringFormatOption<JSONOptions> {

        private StringFormatType formatType = StringFormatType.JSON;

        private JSONOptions() {
        }

        public static JSONOptions json() {
            JSONOptions jsonOptions = new JSONOptions();
            jsonOptions.setPretty(false);
            jsonOptions.setUnprettyArray(false);
            jsonOptions.setDepthSpace("  ");
            jsonOptions.setAllowComments(false);
            jsonOptions.setSkipComments(true);
            jsonOptions.setIgnoreNonNumeric(true);
            jsonOptions.setAllowNaN(true);
            jsonOptions.setAllowPositiveSing(false);
            jsonOptions.setAllowInfinity(true);
            jsonOptions.setAllowUnquoted(true);
            jsonOptions.setAllowSingleQuotes(true);
            jsonOptions.setAllowHexadecimal(true);
            jsonOptions.setLeadingZeroOmission(false);
            //jsonOptions.setAllowCharacter(true);
            jsonOptions.setAllowTrailingComma(true);
            jsonOptions.setAllowConsecutiveCommas(true);
            jsonOptions.setKeyQuote("\"");
            jsonOptions.setValueQuote("\"");
            return jsonOptions;
        }



        public static JSONOptions json5() {
            JSONOptions jsonOptions = new JSONOptions();
            jsonOptions.setPretty(true);
            jsonOptions.setUnprettyArray(true);
            jsonOptions.setDepthSpace("  ");
            jsonOptions.setAllowComments(true);
            jsonOptions.setSkipComments(false);
            jsonOptions.setIgnoreNonNumeric(true);
            jsonOptions.setAllowNaN(true);
            jsonOptions.setAllowPositiveSing(true);
            jsonOptions.setAllowInfinity(true);
            jsonOptions.setAllowUnquoted(true);
            jsonOptions.setAllowSingleQuotes(true);
            jsonOptions.setAllowHexadecimal(true);
            jsonOptions.setLeadingZeroOmission(true);
            //jsonOptions.setAllowCharacter(true);
            jsonOptions.setAllowTrailingComma(true);
            jsonOptions.setAllowBreakLine(true);
            jsonOptions.formatType = StringFormatType.JSON5;
            return jsonOptions;
        }

        private boolean pretty = false;
        private boolean unprettyArray = false;
        private String depthSpace = "  ";
        private boolean skipComments = false;

        private boolean allowComments = false;
        private boolean ignoreNumberFormatError = true;
        private boolean allowNaN = true;
        private boolean allowPositiveSing = true;
        private boolean allowInfinity = true;
        private boolean allowUnquoted = false;
        private boolean allowSingleQuotes = false;
        private boolean allowControlChar = false;

        private boolean allowHexadecimal = true;

        private boolean isLeadingZeroOmission = false;

        //private boolean allowCharacter = false;

        private boolean allowTrailingComma = false;

        private boolean allowBreakLine = false;

        private boolean allowConsecutiveCommas = false;

        private String keyQuote = "\"";
        private String valueQuote = "\"";

        public String getKeyQuote() {
            return keyQuote;
        }

        public JSONOptions setKeyQuote(String keyQuote) {
            if(keyQuote == null)
                throw new IllegalArgumentException("keyQuote can not be null");
            if(keyQuote.length() > 1)
                throw new IllegalArgumentException("keyQuote can not be more than one character");
            this.keyQuote = keyQuote;
            return this;
        }

        public String getValueQuote() {
            return valueQuote;
        }

        @SuppressWarnings("unused")
        public JSONOptions setValueQuote(String valueQuote) {
            if(valueQuote == null || valueQuote.isEmpty())
                throw new IllegalArgumentException("valueQuote can not be null or empty");
            if(valueQuote.length() > 1)
                throw new IllegalArgumentException("valueQuote can not be more than one character");
            this.valueQuote = valueQuote;
            return this;
        }


        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isAllowTrailingComma() {
            return allowTrailingComma;
        }

        public boolean isAllowConsecutiveCommas() {
            return allowConsecutiveCommas;
        }

        public JSONOptions setAllowConsecutiveCommas(boolean allowConsecutiveCommas) {
            this.allowConsecutiveCommas = allowConsecutiveCommas;
            return this;
        }

        public boolean isAllowLineBreak() {
            return allowBreakLine;
        }

        public JSONOptions setAllowBreakLine(boolean allowBreakLine) {
            this.allowBreakLine = allowBreakLine;
            return this;
        }

        public JSONOptions setAllowTrailingComma(boolean allowTrailingComma) {

            this.allowTrailingComma = allowTrailingComma;
            return this;
        }

        public boolean isAllowComments() {
            return allowComments;
        }

        public JSONOptions setAllowComments(boolean allowComments) {
            this.allowComments = allowComments;
            return this;
        }

        public boolean isPretty() {
            return pretty;
        }

        public JSONOptions setPretty(boolean pretty) {
            this.pretty = pretty;
            return this;
        }

        public boolean isUnprettyArray() {
            return unprettyArray;
        }

        public JSONOptions setUnprettyArray(boolean unprettyArray) {
            this.unprettyArray = unprettyArray;
            return this;
        }

        public String getDepthSpace() {
            return depthSpace;
        }

        public JSONOptions setDepthSpace(String depthSpace) {
            if(depthSpace == null) depthSpace = "";
            this.depthSpace = depthSpace;
            return this;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isSkipComments() {
            return skipComments;
        }

        public JSONOptions setSkipComments(boolean allowComment) {
            this.skipComments = allowComment;
            return this;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        @Override
        public boolean isIgnoreNonNumeric() {
            return ignoreNumberFormatError;
        }


        public JSONOptions setIgnoreNonNumeric(boolean ignoreNumberFormatError) {
            this.ignoreNumberFormatError = ignoreNumberFormatError;
            return this;
        }

        public boolean isAllowNaN() {
            return allowNaN;
        }

        public JSONOptions setAllowNaN(boolean allowNaN) {
            this.allowNaN = allowNaN;
            return this;
        }

        public boolean isAllowPositiveSing() {
            return allowPositiveSing;
        }



    public JSONOptions setAllowPositiveSing(boolean allowPositiveSing) {
            this.allowPositiveSing = allowPositiveSing;
            return this;
        }

        public boolean isAllowInfinity() {
            return allowInfinity;
        }

        public JSONOptions setAllowInfinity(boolean allowInfinity) {
            this.allowInfinity = allowInfinity;
            return this;
        }

        public boolean isAllowUnquoted() {
            return this.allowUnquoted;
        }

        public JSONOptions setAllowUnquoted(boolean unquoted) {
            this.allowUnquoted = unquoted;
            this.keyQuote = unquoted ? "" : this.keyQuote;
            return this;
        }

        public boolean isAllowSingleQuotes() {
            return allowSingleQuotes;
        }

        public JSONOptions setAllowSingleQuotes(boolean singleQuotes) {
            this.allowSingleQuotes = singleQuotes;
            this.valueQuote = singleQuotes ? "'" : this.valueQuote;
            return this;
        }

        public boolean isAllowHexadecimal() {
            return allowHexadecimal;
        }

        public JSONOptions setAllowHexadecimal(boolean allowHexadecimal) {
            this.allowHexadecimal = allowHexadecimal;
            return this;
        }

        public boolean isLeadingZeroOmission() {
            return isLeadingZeroOmission;
        }

        public JSONOptions setLeadingZeroOmission(boolean leadingZeroOmission) {
            isLeadingZeroOmission = leadingZeroOmission;
            return this;
        }

        public JSONOptions setAllowControlChar(boolean allowControlChar) {
            this.allowControlChar = allowControlChar;
            return this;
        }

        public boolean isAllowControlChar() {
            return allowControlChar;
        }

        /*public boolean isAllowCharacter() {
            return allowCharacter;
        }*/

        /*public JSONOptions setAllowCharacter(boolean allowCharacter) {
            this.allowCharacter = allowCharacter;
            return this;
        }*/


    @Override
    public StringFormatType getFormatType() {
        return formatType;
    }
}
