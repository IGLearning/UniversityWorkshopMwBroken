package com.iggroup.universityworkshopmw.domain.helpers;

public class Helper {

   public static String createUniqueId(String prefix) {
      return prefix + System.currentTimeMillis();
   }

   public static double roundToTwoDecimalPlaces(double value) {
      return (double) Math.round(value * 100) / 100;
   }
}
