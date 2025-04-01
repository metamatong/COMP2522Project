package ca.bcit.comp2522.project.mygame.util;

import ca.bcit.comp2522.project.mygame.common.MovementDirection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static ca.bcit.comp2522.project.mygame.common.GameConfig.CANVAS_HEIGHT_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CANVAS_WIDTH_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CELL_SIZE_IN_PIXEL;


/**
 * Provides utility methods for drawing text and handling movement direction conversions.
 * <p>
 * This class offers helper methods for computing text width, drawing strings safely within canvas bounds,
 * and converting movement directions into coordinate deltas.
 * </p>
 * @author Kyle Cheon
 * @version 1.0
 */
public class DrawingUtils
{
    private static final int HALVING_FACTOR = 2;
    private static final int BOUNDARY_ZERO = 0;
    private static final int COORDINATE_MOVEMENT_BY_ZERO = 0;
    private static final int COORDINATE_MOVEMENT_BY_NEGATIVE_ONE = -1;
    private static final int COORDINATE_MOVEMENT_BY_POSITIVE_ONE = 1;
    private static final int FIRST_INDEX = 0;

    /**
     * Computes the width of the specified text when rendered with the given font.
     * <p>
     * This method creates a temporary {@link Text} object with the provided text and font, then returns
     * the width of its layout bounds.
     * </p>
     *
     * @param text the text whose width is to be computed.
     * @param font the font used to render the text.
     * @return the width of the text in pixels.
     */
    public static double computeTextWidth(final String text,
                                          final Font font)
    {
        final Text tempText;
        tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getLayoutBounds().getWidth();
    }

    /**
     * Draws a string on the given {@link GraphicsContext} only if the string's position is within the canvas bounds.
     * <p>
     * This method checks if the text drawn at position ({@code x}, {@code y}) with the length based on the
     * {@code CELL_SIZE_IN_PIXEL} multiplied by half the length of the string does not exceed the canvas width or is
     * below zero. If the text is outside the bounds, it is not drawn.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     * @param x  the x-coordinate at which to start drawing the text.
     * @param y  the y-coordinate at which to draw the text.
     * @param s  the string to be drawn.
     */
    public static void putSafeString(final GraphicsContext gc,
                                     final double x,
                                     final double y,
                                     final String s)
    {
        if(x + s.length() * CELL_SIZE_IN_PIXEL / HALVING_FACTOR < BOUNDARY_ZERO || x > CANVAS_WIDTH_IN_PIXEL) return;
        if(y < BOUNDARY_ZERO || y > CANVAS_HEIGHT_IN_PIXEL) return;
        gc.fillText(s,
                    x,
                    y);
    }


    /**
     * Converts the specified movement direction into an x-axis delta.
     * <p>
     * For example, if the direction is {@link MovementDirection#LEFT}, this method returns -1.
     * If the direction is {@link MovementDirection#RIGHT}, it returns 1.
     * For other directions, it returns 0.
     * </p>
     *
     * @param d the movement direction.
     * @return the change in x-coordinate corresponding to the direction.
     */
    public static int directionDeltaX(final MovementDirection d)
    {
        switch(d)
        {
            case LEFT: return COORDINATE_MOVEMENT_BY_NEGATIVE_ONE;
            case RIGHT: return COORDINATE_MOVEMENT_BY_POSITIVE_ONE;
            default: return COORDINATE_MOVEMENT_BY_ZERO;
        }
    }

    /**
     * Converts the specified movement direction into a y-axis delta.
     * <p>
     * For example, if the direction is {@link MovementDirection#UP}, this method returns -1.
     * If the direction is {@link MovementDirection#DOWN}, it returns 1.
     * For other directions, it returns 0.
     * </p>
     *
     * @param d the movement direction.
     * @return the change in y-coordinate corresponding to the direction.
     */
    public static int directionDeltaY(final MovementDirection d)
    {
        switch(d)
        {
            case UP: return COORDINATE_MOVEMENT_BY_NEGATIVE_ONE;
            case DOWN: return COORDINATE_MOVEMENT_BY_POSITIVE_ONE;
            default: return COORDINATE_MOVEMENT_BY_ZERO;
        }
    }

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
        is = DrawingUtils.class.getResourceAsStream(resourcePath);

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
