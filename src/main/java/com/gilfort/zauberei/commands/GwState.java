package com.gilfort.zauberei.commands;

import java.util.HashSet;
import java.util.Set;

/**
 * Per-player runtime state for the gateway command feature.
 */
public class GwState {
    public Set<String> activeTags = new HashSet<>();
    public int tier = 1;
    public int nextInTicks = 20 * 60; // default one minute
}
