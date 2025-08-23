package com.gilfort.zauberei.item.armorbonus;

import org.w3c.dom.Attr;

import java.util.List;
import java.util.Map;

public class ArmorSetData {
    private Map<String, PartData> parts;

    public Map<String, PartData> getParts() {
        return parts;
    }

    public void setParts(Map<String, PartData> parts) {
        this.parts = parts;
    }

    //Inner class for Effects and Attributes for each Equipment Part
    public static class PartData {
        // Gson wird diese Felder per Namen matchen:
        private List<EffectData> Effects;
        private Map<String, AttributeData> Attributes;

        // Getter und Setter (oder public Felder, je nachdem was du bevorzugst)
        public List<EffectData> getEffects() {
            return Effects;
        }

        public void setEffects(List<EffectData> effects) {
            Effects = effects;
        }

        public Map<String, AttributeData> getAttributes() {
            return Attributes;
        }

        public void setAttributes(Map<String, AttributeData> attributes) {
            Attributes = attributes;
        }
    }

    public static class EffectData {
        private String Effect;
        private int Amplifier;

        public int getAmplifier() {
            return Amplifier;
        }

        public void setAmplifier(int amplifier) {
            Amplifier = amplifier;
        }

        public String getEffect() {
            return Effect;
        }

        public void setEffect(String effect) {
            Effect = effect;
        }
    }

    public static class AttributeData{
        private double value;
        private String modifier;

        public double getValue(){return value;}
        public String getModifier(){return modifier;}
    }

}