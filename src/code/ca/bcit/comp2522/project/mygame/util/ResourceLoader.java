package ca.bcit.comp2522.project.mygame.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceLoader
{
    public static String[] loadLogo() throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = ResourceLoader.class.getResourceAsStream("/logo.txt");             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if (is == null) throw new IOException("Logo resource not found!");
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines.toArray(new String[0]);
    }

    public static String[] loadWinLogo() throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = ResourceLoader.class.getResourceAsStream("/winLogo.txt");             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if (is == null) throw new IOException("Logo resource not found!");
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines.toArray(new String[0]);
    }
}
