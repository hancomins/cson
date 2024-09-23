package com.hancomins.cson.options;


@SuppressWarnings("UnusedReturnValue")
public class JsonParsingOptions extends MutableINumberConversionOption<JsonParsingOptions>  implements ParsingOptions<JsonParsingOptions> {

        private StringFormatType formatType = StringFormatType.JSON5;


        private JsonParsingOptions() {
        }


        public static JsonParsingOptions json5() {
            JsonParsingOptions jsonParsingOptions = new JsonParsingOptions();
            jsonParsingOptions.setIgnoreNonNumeric(true);
            jsonParsingOptions.setAllowNaN(true);
            jsonParsingOptions.setAllowPositiveSing(true);
            jsonParsingOptions.setAllowInfinity(true);
            jsonParsingOptions.setAllowHexadecimal(true);
            jsonParsingOptions.setLeadingZeroOmission(true);
            jsonParsingOptions.formatType = StringFormatType.JSON5;
            return jsonParsingOptions;
        }


        private boolean allowConsecutiveCommas = false;


        public boolean isAllowConsecutiveCommas() {
            return allowConsecutiveCommas;
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
