package com.hancomins.cson.options;


@SuppressWarnings("UnusedReturnValue")
public class JsonParsingOptions extends MutableINumberConversionOption<JsonParsingOptions>  implements ParsingOptions<JsonParsingOptions> {

        private StringFormatType formatType = StringFormatType.JSON5;

        private boolean allowUnquoted = true;
        private boolean allowComment = true;


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
            jsonParsingOptions.setAllowComment(true);
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

        public boolean isAllowComment() {
            return allowComment;
        }

        public JsonParsingOptions setAllowUnquoted(boolean allowUnquoted) {
            this.allowUnquoted = allowUnquoted;
            return this;
        }

        public JsonParsingOptions setAllowComment(boolean allowComment) {
            this.allowComment = allowComment;
            return this;
        }

        public JsonParsingOptions setAllowConsecutiveCommas(boolean allowConsecutiveCommas) {
            this.allowConsecutiveCommas = allowConsecutiveCommas;
            return this;
        }



    @Override
    public StringFormatType getFormatType() {
        return formatType;
    }
}
