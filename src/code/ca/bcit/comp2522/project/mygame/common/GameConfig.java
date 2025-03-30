package ca.bcit.comp2522.project.mygame.common;


public final class GameConfig
{
    public static final int GRID_WIDTH = 50;
    public static final int GRID_HEIGHT = 40;
    public static final int CELL_SIZE = 20; // pixel size per grid cell
    public static final int TOP_MARGIN = 10;
    public static final int BOTTOM_MARGIN = 20;
    public static final int CANVAS_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * CELL_SIZE + TOP_MARGIN + BOTTOM_MARGIN;
    // Height for the stats display (you can adjust as needed)
    public static final int STAT_HEIGHT = CELL_SIZE;
    public static final int FINISH_LINE_Y = 5; // Change from 0 to 5 (or another value) so players finish sooner.
    public static final long MOVE_COOLDOWN = 400_000_000;
}
