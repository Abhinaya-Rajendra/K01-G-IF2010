package com.nimonscooked.core;

public class SoundManager {
    private static SoundManager instance;
    
    // Placeholder method
    public void playMusic(String key) { /* Logic play BGM */ }
    public void stopMusic() { /* Logic stop */ }
    
    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }
}