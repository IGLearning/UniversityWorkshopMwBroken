package com.iggroup.universityworkshopmw.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarketName {

   GOLD("Gold", 500.9),
   SILVER("Silver", 375.2),
   PLATINUM("Platinum", 312.0),
   COPPER("Copper", 250.0),
   NATURAL_GAS("Natural Gas", 300.7),
   COFFEE("Coffee", 205.0),
   WHEAT("Wheat", 175.4),
   COCOA("Cocoa", 225.1),
   COTTON("Cotton", 125.0),
   SUGAR("Sugar", 148.0);

   private final String name;
   private final double startingPrice;
}
