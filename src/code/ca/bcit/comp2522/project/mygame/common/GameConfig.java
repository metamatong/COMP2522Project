package ca.bcit.comp2522.project.mygame.common;


/**
 * A final class that contains configuration constants for the game.
 * <p>
 * This class defines various static constants used to configure the game grid dimensions,
 * cell size, canvas dimensions, margins, display statistics, finish line position, and the
 * movement cooldown duration.
 * </p>
 * @author Kyle Cheon
 * @version 1.0
 */
public final class GameConfig
{
    public static final int GRID_WIDTH = 50;
    public static final int GRID_HEIGHT = 40;
    public static final int TOP_MARGIN = 10;
    public static final int BOTTOM_MARGIN = 20;

    /**
     * The size of each grid cell in pixels.
     */
    public static final int CELL_SIZE_IN_PIXEL = 20;

    /**
     * The width of the game canvas in pixels, calculated as GRID_WIDTH * CELL_SIZE.
     */
    public static final int CANVAS_WIDTH_IN_PIXEL = GRID_WIDTH * CELL_SIZE_IN_PIXEL;

    /**
     * The height of the game canvas in pixels, calculated as (GRID_HEIGHT * CELL_SIZE) + TOP_MARGIN + BOTTOM_MARGIN.
     */
    public static final int CANVAS_HEIGHT_IN_PIXEL = GRID_HEIGHT * CELL_SIZE_IN_PIXEL + TOP_MARGIN + BOTTOM_MARGIN;

    /**
     * The height reserved for the stats display in pixels.
     */
    public static final int STAT_HEIGHT_IN_PIXEL = CELL_SIZE_IN_PIXEL;

    /**
     * The Y-coordinate of the finish line on the game grid.
     * <p>
     * Adjusting this value (e.g., from 0 to 5) makes the finish line appear sooner in the game.
     * </p>
     */
    public static final int FINISH_LINE_Y = 5;

    /**
     * The cooldown duration between moves in nanoseconds.
     */
    public static final long MOVE_COOLDOWN_NANOSECONDS = 400_000_000;
}
