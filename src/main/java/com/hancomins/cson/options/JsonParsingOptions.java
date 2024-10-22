package com.hancomins.cson.options;


@SuppressWarnings("UnusedReturnValue")
public class JsonParsingOptions extends MutableINumberConversionOption<JsonParsingOptions>  implements ParsingOptions<JsonParsingOptions> {

        private StringFormatType formatType = StringFormatType.JSON5;

        private boolean allowUnquoted = true;
        private boolean allowComments = true;
        private boolean ignoreTrailingData = false;
        private boolean skipComments = false;
        private boolean allowControlCharacters = false;
        private boolean ignoreControlCharacters = true;

        private JsonParsingOptions() {
        }


        public static JsonParsingOptions json5() {
            JsonParsingOptions jsonParsingOptions = new JsonParsingOptions();
            jsonParsingOptions.setIgnoreNonNumeric(false);
            jsonParsingOptions.setAllowNaN(true);
            jsonParsingOptions.setAllowPositiveSing(true);
            jsonParsingOptions.setAllowInfinity(true);
            jsonParsingOptions.setAllowHexadecimal(true);
            jsonParsingOptions.setLeadingZeroOmission(true);
            jsonParsingOptions.setAllowUnquoted(true);
            jsonParsingOptions.setAllowComments(true);
            jsonParsingOptions.formatType = StringFormatType.JSON5;
            return jsonParsingOptions;
        }

    public static JsonParsingOptions json() {
        JsonParsingOptions jsonParsingOptions = new JsonParsingOptions();
        jsonParsingOptions.setIgnoreNonNumeric(false);
        jsonParsingOptions.setAllowNaN(false);
        jsonParsingOptions.setAllowPositiveSing(false);
        jsonParsingOptions.setAllowInfinity(false);
        jsonParsingOptions.setAllowHexadecimal(true);
        jsonParsingOptions.setLeadingZeroOmission(false);
        jsonParsingOptions.setAllowUnquoted(false);
        jsonParsingOptions.setAllowComments(false);
        jsonParsingOptions.setAllowControlCharacters(false);
        jsonParsingOptions.formatType = StringFormatType.JSON5;
        return jsonParsingOptions;
    }


        private boolean allowConsecutiveCommas = false;


        public boolean isAllowConsecutiveCommas() {
            return allowConsecutiveCommas;
        }

        public boolean isAllowUnquoted() {
            return allowUnquoted;
        }

        public boolean isAllowComments() {
            return allowComments;
        }

        public JsonParsingOptions setAllowUnquoted(boolean allowUnquoted) {
            this.allowUnquoted = allowUnquoted;
            return this;
        }

        public JsonParsingOptions setSkipComments(boolean skipComments) {
            this.skipComments = skipComments;
            return this;
        }

        public boolean isSkipComments() {
            return skipComments;
        }

        public boolean isAllowControlCharacters() {
            return allowControlCharacters;
        }

        public JsonParsingOptions setAllowControlCharacters(boolean allowControlCharacters) {
            this.allowControlCharacters = allowControlCharacters;
            return this;
        }

        public boolean isIgnoreControlCharacters() {
            return ignoreControlCharacters;
        }

        public JsonParsingOptions setIgnoreControlCharacters(boolean ignoreControlCharacters) {
            this.ignoreControlCharacters = ignoreControlCharacters;
            return this;
        }


        public JsonParsingOptions setAllowComments(boolean allowComments) {
            this.allowComments = allowComments;
            return this;
        }

        public JsonParsingOptions setAllowConsecutiveCommas(boolean allowConsecutiveCommas) {
            this.allowConsecutiveCommas = allowConsecutiveCommas;
            return this;
        }

        public boolean isIgnoreTrailingData() {
            return ignoreTrailingData;
        }

        public JsonParsingOptions setIgnoreTrailingData(boolean ignoreTrailingData) {
            this.ignoreTrailingData = ignoreTrailingData;
            return this;
        }



    @Override
    public StringFormatType getFormatType() {
        return formatType;
    }
}
