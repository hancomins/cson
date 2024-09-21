package com.hancomins.cson;


import com.hancomins.cson.options.ParsingOption;
import com.hancomins.cson.options.StringFormatType;

@SuppressWarnings("UnusedReturnValue")
public class JSONParsingOptions implements ParsingOption<JSONParsingOptions> {

        private StringFormatType formatType = StringFormatType.JSON;



        private JSONParsingOptions() {
        }


        static boolean isPureJSONOption(ParsingOption<?> parsingOption) {
            if(!(parsingOption instanceof JSONParsingOptions))
                return false;

            JSONParsingOptions jsonParsingOptions = (JSONParsingOptions) parsingOption;
            return
                    // 코멘트 사용 불가
                    !jsonParsingOptions.isAllowComments() &&
                    // 싱글 쿼트 사용 불가
                    !jsonParsingOptions.isAllowSingleQuotes() &&
                    // 값과 } , ] 사이에 comma 사용 불가s
                    !jsonParsingOptions.isAllowTrailingComma() &&
                    // 연이은 comma 사용 불가
                    !jsonParsingOptions.isAllowConsecutiveCommas() &&
                    // 키, 값 쿼트 생략 불가
                    !jsonParsingOptions.isAllowUnquoted() &&
                    // key 쿼트 " 만 사용
                    "\"".equals(jsonParsingOptions.getKeyQuote()) &&
                    // value 쿼트 " 만 사용
                    "\"".equals(jsonParsingOptions.getValueQuote());



        }




        public static JSONParsingOptions json() {
            JSONParsingOptions jsonParsingOptions = new JSONParsingOptions();
            jsonParsingOptions.setPretty(false);
            jsonParsingOptions.setUnprettyArray(false);
            jsonParsingOptions.setDepthSpace("  ");
            jsonParsingOptions.setAllowComments(false);
            jsonParsingOptions.setSkipComments(true);
            jsonParsingOptions.setIgnoreNonNumeric(true);

            jsonParsingOptions.setAllowNaN(false);
            jsonParsingOptions.setAllowPositiveSing(false);
            jsonParsingOptions.setAllowInfinity(false);
            jsonParsingOptions.setAllowUnquoted(false);
            jsonParsingOptions.setAllowTrailingComma(false);

            jsonParsingOptions.setAllowSingleQuotes(false);
            jsonParsingOptions.setAllowHexadecimal(true);
            jsonParsingOptions.setLeadingZeroOmission(true);
            jsonParsingOptions.setAllowPositiveSing(true);



            jsonParsingOptions.setAllowConsecutiveCommas(false);
            jsonParsingOptions.setKeyQuote("\"");
            jsonParsingOptions.setValueQuote("\"");
            jsonParsingOptions.setPretty(false);
            jsonParsingOptions.setUnprettyArray(true);

            return jsonParsingOptions;
        }



        public static JSONParsingOptions json5() {
            JSONParsingOptions jsonParsingOptions = new JSONParsingOptions();
            jsonParsingOptions.setPretty(true);
            jsonParsingOptions.setUnprettyArray(true);
            jsonParsingOptions.setDepthSpace("  ");
            jsonParsingOptions.setAllowComments(true);
            jsonParsingOptions.setSkipComments(false);
            jsonParsingOptions.setIgnoreNonNumeric(true);
            jsonParsingOptions.setAllowNaN(true);
            jsonParsingOptions.setAllowPositiveSing(true);
            jsonParsingOptions.setAllowInfinity(true);
            jsonParsingOptions.setAllowUnquoted(true);
            jsonParsingOptions.setAllowSingleQuotes(true);
            jsonParsingOptions.setAllowHexadecimal(true);
            jsonParsingOptions.setLeadingZeroOmission(true);

            jsonParsingOptions.setAllowTrailingComma(true);
            jsonParsingOptions.setAllowBreakLine(true);
            jsonParsingOptions.formatType = StringFormatType.JSON5;
            return jsonParsingOptions;
        }

        private boolean pretty = false;
        private boolean unprettyArray = false;
        private String depthSpace = "  ";
        private boolean skipComments = false;

        private boolean allowComments = false;
        // todo : 이 옵션을 제거할 수 있는지 검토
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

        public JSONParsingOptions setKeyQuote(String keyQuote) {
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

        @SuppressWarnings({"unused", "SameParameterValue"})
        JSONParsingOptions setValueQuote(String valueQuote) {
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

        public JSONParsingOptions setAllowConsecutiveCommas(boolean allowConsecutiveCommas) {
            this.allowConsecutiveCommas = allowConsecutiveCommas;
            return this;
        }

        public boolean isAllowLineBreak() {
            return allowBreakLine;
        }

        public JSONParsingOptions setAllowBreakLine(boolean allowBreakLine) {
            this.allowBreakLine = allowBreakLine;
            return this;
        }

        public JSONParsingOptions setAllowTrailingComma(boolean allowTrailingComma) {

            this.allowTrailingComma = allowTrailingComma;
            return this;
        }

        public boolean isAllowComments() {
            return allowComments;
        }

        public JSONParsingOptions setAllowComments(boolean allowComments) {
            this.allowComments = allowComments;
            return this;
        }

        public boolean isPretty() {
            return pretty;
        }

        public JSONParsingOptions setPretty(boolean pretty) {
            this.pretty = pretty;
            return this;
        }

        public boolean isUnprettyArray() {
            return unprettyArray;
        }

        public JSONParsingOptions setUnprettyArray(boolean unprettyArray) {
            this.unprettyArray = unprettyArray;
            return this;
        }

        public String getDepthSpace() {
            return depthSpace;
        }

        public JSONParsingOptions setDepthSpace(String depthSpace) {
            if(depthSpace == null) depthSpace = "";
            this.depthSpace = depthSpace;
            return this;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isSkipComments() {
            return skipComments;
        }

        public JSONParsingOptions setSkipComments(boolean allowComment) {
            this.skipComments = allowComment;
            return this;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        @Override
        public boolean isIgnoreNonNumeric() {
            return ignoreNumberFormatError;
        }


        public JSONParsingOptions setIgnoreNonNumeric(boolean ignoreNumberFormatError) {
            this.ignoreNumberFormatError = ignoreNumberFormatError;
            return this;
        }

        public boolean isAllowNaN() {
            return allowNaN;
        }

        public JSONParsingOptions setAllowNaN(boolean allowNaN) {
            this.allowNaN = allowNaN;
            return this;
        }

        public boolean isAllowPositiveSing() {
            return allowPositiveSing;
        }



    public JSONParsingOptions setAllowPositiveSing(boolean allowPositiveSing) {
            this.allowPositiveSing = allowPositiveSing;
            return this;
        }

        public boolean isAllowInfinity() {
            return allowInfinity;
        }

        public JSONParsingOptions setAllowInfinity(boolean allowInfinity) {
            this.allowInfinity = allowInfinity;
            return this;
        }

        public boolean isAllowUnquoted() {
            return this.allowUnquoted;
        }

        public JSONParsingOptions setAllowUnquoted(boolean unquoted) {
            this.allowUnquoted = unquoted;
            this.keyQuote = unquoted ? "" : this.keyQuote;
            return this;
        }

        public boolean isAllowSingleQuotes() {
            return allowSingleQuotes;
        }

        public JSONParsingOptions setAllowSingleQuotes(boolean singleQuotes) {
            this.allowSingleQuotes = singleQuotes;
            this.valueQuote = singleQuotes ? "'" : this.valueQuote;
            return this;
        }

        public boolean isAllowHexadecimal() {
            return allowHexadecimal;
        }

        public JSONParsingOptions setAllowHexadecimal(boolean allowHexadecimal) {
            this.allowHexadecimal = allowHexadecimal;
            return this;
        }

        public boolean isLeadingZeroOmission() {
            return isLeadingZeroOmission;
        }

        public JSONParsingOptions setLeadingZeroOmission(boolean leadingZeroOmission) {
            isLeadingZeroOmission = leadingZeroOmission;
            return this;
        }

        public JSONParsingOptions setAllowControlChar(boolean allowControlChar) {
            this.allowControlChar = allowControlChar;
            return this;
        }

        public boolean isAllowControlChar() {
            return allowControlChar;
        }


    @Override
    public StringFormatType getFormatType() {
        return formatType;
    }
}
