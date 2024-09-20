package com.hancomins.cson.options;

public interface NumberConversionOption {
     boolean isAllowNaN();
     boolean isAllowInfinity();
     boolean isAllowHexadecimal();
     boolean isLeadingZeroOmission();
     boolean isAllowPositiveSing();
     boolean isIgnoreNonNumeric();

     NumberConversionOption DEFAULT =    new NumberConversionOption() {
          @Override
          public boolean isAllowNaN() {
               return true;
          }

          @Override
          public boolean isAllowInfinity() {
               return true;
          }

          @Override
          public boolean isAllowHexadecimal() {
               return true;
          }

          @Override
          public boolean isLeadingZeroOmission() {
               return true;
          }

          @Override
          public boolean isAllowPositiveSing() {
               return true;
          }

          @Override
          public boolean isIgnoreNonNumeric() {
               return true;
          }
     };

}