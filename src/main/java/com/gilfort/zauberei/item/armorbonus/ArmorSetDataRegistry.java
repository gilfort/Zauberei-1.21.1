package com.gilfort.zauberei.item.armorbonus;

import java.util.HashMap;
import java.util.Map;

public class ArmorSetDataRegistry {

    // Hier werden alle Daten gesammelt
    private static final Map<String, ArmorSetData> DATA_MAP = new HashMap<>();

    // Der Reload Listener füllt diese Map nach dem apply()
    public static void put(String major, int year, String armorMaterial, ArmorSetData data) {
        String key = major + ":" + year + ":" + armorMaterial;
        DATA_MAP.put(key, data);
    }

    public static ArmorSetData getData(String major, int year, String armorMaterial) {
        String key = major + ":" + year + ":" + armorMaterial;
        return DATA_MAP.get(key);
    }

    // Test-/Debug-Methode
    public static void debugPrintData(String major, int year, String material) {
        ArmorSetData data = getData(major, year, material);
        if (data == null) {
            System.out.println("No data found for " + major + ", " + year + ", " + material);
            return;
        }

        // Durch alle Parts iterieren
        data.getParts().forEach((partKey, partData) -> {
            System.out.println("Part: " + partKey);

            System.out.println("  Effects:");
            if (partData.getEffects() != null) {
                for (ArmorSetData.EffectData effect : partData.getEffects()) {
                    System.out.println("    - " + effect.getEffect() + " (Amplifier: " + effect.getAmplifier() + ")");
                }
            }

            System.out.println("  Attributes:");
            if (partData.getAttributes() != null) {
                partData.getAttributes().forEach((attrKey, value) ->
                        System.out.println("    - " + attrKey + " = " + value)
                );
            }
        });
    }

    private static String makeKey(String major, int year, String material) {
        return major + ":" + year + ":" + material;
    }
}
