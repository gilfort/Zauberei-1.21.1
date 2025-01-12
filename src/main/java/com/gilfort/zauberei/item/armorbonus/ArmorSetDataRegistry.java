//package com.gilfort.zauberei.item.armorbonus;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class ArmorSetDataRegistry {
//
//    // Hier werden alle Daten gesammelt
//    private static final Map<String, ArmorSetData> DATA_MAP = new HashMap<>();
//
//    // Der Reload Listener füllt diese Map nach dem apply()
//    public static void put(String major, int year, String armorMaterial, ArmorSetData data) {
//        String key = major + ":" + year + ":" + armorMaterial;
//        DATA_MAP.put(key, data);
//    }
//
//    public static ArmorSetData getData(String major, int year, String armorMaterial) {
//        String key = major + ":" + year + ":" + armorMaterial;
//        return DATA_MAP.get(key);
//    }
//
//}
