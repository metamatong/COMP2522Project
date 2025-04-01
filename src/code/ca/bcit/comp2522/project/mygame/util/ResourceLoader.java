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
     * Loads a text resource from the classpath and returns its content as an array of strings.
     * <p>
     * This method reads the specified resource file from the classpath line by line and returns an array of strings,
     * where each string represents a line from the file. It can be used to load ASCII art logos or any other text-based resource.
     * For example, to load the logo file, pass "/logo.txt", or to load the winning logo, pass "/winLogo.txt".
     * </p>
     *
     * @param resourcePath the path of the resource to load (e.g. "/logo.txt" or "/winLogo.txt")
     * @return an array of strings representing the content of the resource.
     * @throws IOException if the resource is not found or an I/O error occurs during reading.
     */
    public static String[] loadResource(final String resourcePath)
            throws IOException
    {
        final InputStream is;
        is = ResourceLoader.class.getResourceAsStream(resourcePath);

        if(is == null)
        {
            throw new IOException("Resource not found: " + resourcePath);
        }

        final List<String> lines;
        lines = new ArrayList<>();

        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        return lines.toArray(new String[FIRST_INDEX]);
    }
}
