package com.sigma48.util;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundUtils {

    private static final Map<String, AudioClip> audioCache = new HashMap<>();

    public static void playSound(String soundFileName) {
        if (soundFileName == null || soundFileName.isEmpty()) {
            System.err.println("Nama file suara kosong atau null.");
            return;
        }

        try {
            AudioClip clip = audioCache.computeIfAbsent(soundFileName, name -> {
                URL resource = SoundUtils.class.getResource("/com/sigma48/sounds/" + name);
                if (resource == null) {
                    System.err.println("File suara tidak ditemukan: " + name);
                    return null;
                }
                return new AudioClip(resource.toExternalForm());
            });

            if (clip != null) {
                clip.play();
            }

        } catch (Exception e) {
            System.err.println("Error saat memutar suara " + soundFileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
