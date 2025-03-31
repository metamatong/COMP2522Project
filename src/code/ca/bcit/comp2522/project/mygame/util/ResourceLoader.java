package ca.bcit.comp2522.project.mygame.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods to load text-based resources from the classpath.
 * <p>
 * This class contains methods to load logo files as arrays of strings, which are used
 * for displaying ASCII art in the game's user interface.
 * </p>
 * @author Kyle Cheon
 * @version 1.0
 */
public class ResourceLoader
{
    private static final int FIRST_INDEX = 0;

    /**
     * Loads the logo resource from the "/logo.txt" file.
     * <p>
     * This method reads the "/logo.txt" file from the classpath line by line and returns an array of strings,
     * where each string represents a line of the logo.
     * </p>
     *
     * @return an array of strings representing the logo.
     * @throws IOException if the logo resource is not found or an I/O error occurs during reading.
     */
    public static String[] loadLogo()
            throws IOException
    {
        final List<String> lines;
        lines = new ArrayList<>();
        try(final InputStream is = ResourceLoader.class.getResourceAsStream("/logo.txt");             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if(is == null) throw new IOException("Logo resource not found!");
            String line;
            while((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        return lines.toArray(new String[FIRST_INDEX]);
    }

    /**
     * Loads the winning logo resource from the "/winLogo.txt" file.
     * <p>
     * This method reads the "/winLogo.txt" file from the classpath line by line and returns an array of strings,
     * where each string represents a line of the winning logo.
     * </p>
     *
     * @return an array of strings representing the win logo.
     * @throws IOException if the win logo resource is not found or an I/O error occurs during reading.
     */
    public static String[] loadWinLogo()
            throws IOException
    {
        final List<String> lines;
        lines = new ArrayList<>();
        try(final InputStream is = ResourceLoader.class.getResourceAsStream("/winLogo.txt");             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if(is == null) throw new IOException("Logo resource not found!");
            String line;
            while((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        return lines.toArray(new String[FIRST_INDEX]);
    }
}
