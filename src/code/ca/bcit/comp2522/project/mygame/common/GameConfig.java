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
    public static final int NUMBER_OF_PLAYERS = 45;
    public static final int ALLOWED_NUMBER_OF_WINNERS = 10;

    public static final int LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS = 1500;
    public static final int LIGHT_SWITCH_FROM_RED_LIGHT_MINIMUM_INTERVAL_IN_MILLISECONDS = 3000;
    public static final int LIGHT_SWITCH_TIME_VARIABILITY_IN_MILLISECONDS = 2500;

    public static final double NPC_MOVING_SPEED_PROBABILITY = 0.1;
    public static final int NPC_MOVING_DISTANCE_ON_X_AXIS = 0;
    public static final int NPC_MOVING_DISTANCE_ON_Y_AXIS = -1;
    public static final double NPC_MOVING_IN_RED_LIGHT_PROBABILITY = 0.001;

    public static final int GRID_WIDTH_IN_NUMBER_OF_CELLS = 50;
    public static final int GRID_HEIGHT_IN_NUMBER_OF_CELLS = 40;
    public static final int TOP_MARGIN_IN_PIXEL = 10;
    public static final int BOTTOM_MARGIN_IN_PIXEL = 20;

    /**
     * The bottom offset for determining where player starts the game.
     */
    public static final int BOTTOM_OFFSET_IN_NUMBER_OF_CELLS = 2;

    /**
     * The size of each grid cell in pixels.
     */
    public static final int CELL_SIZE_IN_PIXEL = 20;

    /**
     * The width of the game canvas in pixels, calculated as GRID_WIDTH * CELL_SIZE.
     */
    public static final int CANVAS_WIDTH_IN_PIXEL = GRID_WIDTH_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL;

    /**
     * The height of the game canvas in pixels, calculated as (GRID_HEIGHT * CELL_SIZE) + TOP_MARGIN + BOTTOM_MARGIN.
     */
    public static final int CANVAS_HEIGHT_IN_PIXEL = GRID_HEIGHT_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL +
                            TOP_MARGIN_IN_PIXEL + BOTTOM_MARGIN_IN_PIXEL;

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
    public static final int FINISH_LINE_Y_IN_NUMBER_OF_CELLS = 5;

    /**
     * The cooldown duration between moves in nanoseconds.
     */
    public static final long MOVE_COOLDOWN_IN_NANOSECONDS = 400_000_000;

    /**
     * This is private constructor of the GameConfig class.
     * Setting it private is to prevent instantiation of this class.
     */
    private GameConfig() {}
}
