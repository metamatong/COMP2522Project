package ca.bcit.comp2522.project.mygame.ui;

import ca.bcit.comp2522.project.mygame.common.GameState;
import ca.bcit.comp2522.project.mygame.engine.GameLogic;
import ca.bcit.comp2522.project.mygame.entities.Player;
import ca.bcit.comp2522.project.mygame.util.ResourceLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.IOException;

import static ca.bcit.comp2522.project.mygame.common.GameConfig.CANVAS_HEIGHT_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CANVAS_WIDTH_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CELL_SIZE_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.FINISH_LINE_Y_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.STAT_HEIGHT_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.TOP_MARGIN_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.computeTextWidth;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.putSafeString;

/**
 * Draws the user interface for the game.
 * <p>
 * This class is responsible for rendering the different screens and UI elements of the game,
 * including the intro screen, in-game display (with player sprites, statistics, and light machines),
 * and the game over screen. It uses a {@link GameLogic} instance to obtain game state information and
 * renders the graphics using a JavaFX {@link GraphicsContext}.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class GameRenderer
{
    private static final int COORDINATE_ZERO = 0;
    private static final int FIRST_INDEX = 0;
    private static final double INTRO_LOGO_CHARACTER_SIZE_WEIGHT = 0.75;
    private static final double GAME_STAT_CHARACTER_SIZE_WEIGHT = 0.6;
    private static final int LOGO_MIDDLE_FACTOR = 2;
    private static final double NANOSECOND_PER_SECOND = 1_000_000_000.0;
    private static final int INITIAL_COUNT = 0;
    private static final int GAME_STATS_OFFSET_IN_NUMBER_OF_CELLS = 5;
    private static final String FONT_STYLE = "Monospaced";
    private static final int FINISH_LINE_WIDTH_IN_PIXEL = 1;
    private static final double LIGHT_MACHINE_SIZE_WEIGHT = 0.6;
    private static final int SPRITE_OFFSET_ONE = 1;
    private static final int SPRITE_OFFSET_TWO = 2;
    private static final int SPRITE_OFFSET_THREE = 3;
    private static final int SPRITE_OFFSET_FOUR = 4;
    private static final String SPRITE_HEAD = " O ";
    private static final String SPRITE_UPPER_BODY = "/|\\";
    private static final String SPRITE_CORE_BODY = " | ";
    private static final String SPRITE_LOWER_BODY = "/ \\";

    private String[] logoLines;
    private String[] logoWinLines;
    private final GameLogic gameLogic;

    /**
     * Constructs a new GameRenderer with the specified GameLogic instance.
     * <p>
     * This constructor attempts to load the logo assets for the intro and winning screens using
     * the {@link ResourceLoader}. If the assets cannot be loaded, fallback default strings are used.
     * </p>
     *
     * @param gameLogic the GameLogic instance used to retrieve game state information.
     */
    public GameRenderer(final GameLogic gameLogic)
    {
        validateGameLogic(gameLogic);
        this.gameLogic = gameLogic;

        try
        {
            logoLines = ResourceLoader.loadResource("/logo.txt");
        }
        catch
        (final IOException e)
        {
            logoLines = new String[]{"RED LIGHT", "BLOOD LIGHT"};
        }

        try
        {
            logoWinLines = ResourceLoader.loadResource("/winLogo.txt");
        }
        catch
        (final IOException e)
        {
            logoWinLines = new String[]
                    {
                        "YOU WIN!",
                        "(No fancy win logo found.)"
                    };
        }
    }

    /**
     * Renders the game screen based on the current game state.
     * <p>
     * Depending on the provided {@code state}, this method delegates to one of the specific
     * screen drawing methods: intro screen, in-game screen, or game over screen.
     * </p>
     *
     * @param state the current game state.
     * @param gc    the GraphicsContext used for drawing.
     */
    public void render(final GameState state,
                       final GraphicsContext gc)
    {
        switch(state)
        {
            case INTRO:
                drawIntroScreen(gc);
                break;
            case GAME:
                drawGame(gc);
                break;
            case GAME_OVER:
                drawGameOverScreen(gc);
                break;
        }
    }

    /*
     * Draws the intro screen.
     * <p>
     * This method clears the canvas, draws the intro logo, title, and user instructions
     * for starting or exiting the game.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     */
    private void drawIntroScreen(final GraphicsContext gc)
    {
        gc.setFill(Color.BLACK);
        gc.fillRect(COORDINATE_ZERO,
                    COORDINATE_ZERO,
                    CANVAS_WIDTH_IN_PIXEL,
                    CANVAS_HEIGHT_IN_PIXEL);
        gc.setFill(Color.WHITE);

        // Draw intro ASCII logo image.
        gc.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT));

        final int logoHeight;
        final double logoY;

        logoHeight = logoLines.length;
        logoY = CANVAS_HEIGHT_IN_PIXEL / LOGO_MIDDLE_FACTOR -
                (logoHeight * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT) / LOGO_MIDDLE_FACTOR -
                (CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT);

        for(int i = 0; i < logoLines.length; i++)
        {
            final String line;
            final double textWidth;
            final double x;
            line = logoLines[i];
            textWidth = computeTextWidth(line, gc.getFont());
            x = (CANVAS_WIDTH_IN_PIXEL - textWidth) / LOGO_MIDDLE_FACTOR;
            gc.fillText(line,
                        x,
                    logoY + i * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT);
        }

        // Construct title sentence.
        gc.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL)); // Back to original font size for title
        final String titlePart1;
        final String titlePart2;
        final String redWord;
        final String fullTitle;

        titlePart1 = "WELCOME TO RED LIGHT ";
        titlePart2 = " LIGHT";
        redWord = "BLOOD";
        fullTitle = titlePart1 + redWord + titlePart2;

        final double titleY;
        final double titleWidth;
        final double titleX;
        double offset;

        titleY = logoY + logoHeight * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT + CELL_SIZE_IN_PIXEL;
        titleWidth = computeTextWidth(fullTitle, gc.getFont());
        titleX = (CANVAS_WIDTH_IN_PIXEL - titleWidth) / LOGO_MIDDLE_FACTOR;

        gc.setFill(Color.WHITE);
        gc.fillText(titlePart1,
                    titleX,
                    titleY);
        offset = computeTextWidth(titlePart1, gc.getFont());
        gc.setFill(Color.RED);
        gc.fillText(redWord,
                    titleX + offset,
                    titleY);
        offset += computeTextWidth(redWord, gc.getFont());
        gc.setFill(Color.WHITE);
        gc.fillText(titlePart2,
                    titleX + offset,
                    titleY);

        // Draw instruction sentence for the user.
        final String instructions;
        final double instrWidth;
        final double instrX;
        final double instrY;

        instructions = "Press ENTER to PLAY or ESC to EXIT";
        instrWidth = computeTextWidth(instructions, gc.getFont());
        instrX = (CANVAS_WIDTH_IN_PIXEL - instrWidth) / LOGO_MIDDLE_FACTOR;
        instrY = titleY + CELL_SIZE_IN_PIXEL * LOGO_MIDDLE_FACTOR;
        gc.fillText(instructions, instrX, instrY);
    }

    /*
     * Draws the in-game screen.
     * <p>
     * This method clears the previous game statistics, calculates the elapsed time and dead player count,
     * and then renders the game statistics, light machine (red or green), finish line, and player sprites.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     */
    private void drawGame(final GraphicsContext gc)
    {
        // Compute elapsed time in seconds.
        final double elapsedSeconds;
        elapsedSeconds = (System.nanoTime() - gameLogic.getGameStartTimeInNanoseconds()) / NANOSECOND_PER_SECOND;

        // Count dead players.
        int deadCount = INITIAL_COUNT;
        for(final Player p : gameLogic.getPlayers())
        {
            if(p.isEliminated())
            {
                deadCount++;
            }
        }

        // Clear previous stats
        gc.setFill(Color.BLACK);
        gc.fillRect(COORDINATE_ZERO,
                    TOP_MARGIN_IN_PIXEL,
                    CANVAS_WIDTH_IN_PIXEL,
                    STAT_HEIGHT_IN_PIXEL);

        // Prepare the stats string.
        String stats = String.format("Time: %.1fs   Finished: %d   Dead: %d",
                elapsedSeconds, gameLogic.getFinishedCount(), deadCount);

        // Draw the stats at the top left (using a smaller font so it fits nicely).
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font(FONT_STYLE,
                CELL_SIZE_IN_PIXEL * GAME_STAT_CHARACTER_SIZE_WEIGHT));
        gc.fillText(stats,
                GAME_STATS_OFFSET_IN_NUMBER_OF_CELLS,
                TOP_MARGIN_IN_PIXEL + CELL_SIZE_IN_PIXEL / LOGO_MIDDLE_FACTOR);

        gc.setFill(Color.BLACK);
        // Clear only below the header area.
        gc.fillRect(COORDINATE_ZERO,
                TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL,
                CANVAS_WIDTH_IN_PIXEL,
                CANVAS_HEIGHT_IN_PIXEL - TOP_MARGIN_IN_PIXEL - STAT_HEIGHT_IN_PIXEL);

        gc.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL));
        gc.setTextAlign(TextAlignment.LEFT);
        if(!gameLogic.isGreen())
        {
            drawLightMachine(gc);
        }
        else
        {
            drawGreenMachine(gc);
        }

        double finishLineYCanvas = FINISH_LINE_Y_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL +
                                   TOP_MARGIN_IN_PIXEL +
                                   STAT_HEIGHT_IN_PIXEL;

        // Set the stroke properties.
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(FINISH_LINE_WIDTH_IN_PIXEL);

        // Draw a horizontal line across the canvas.
        gc.strokeLine(COORDINATE_ZERO,
                      finishLineYCanvas,
                      CANVAS_WIDTH_IN_PIXEL,
                      finishLineYCanvas);

        for(final Player p : gameLogic.getPlayers())
        {
            // Skip finished players so they no longer appear on-screen.
            if (p.isFinished()) continue;
            if (p.isEliminated())
            {
                drawDeadSprite(gc, p);
            }
            else
            {
                drawPlayerSprite(gc, p);
            }
        }
    }

    /*
     * Draws the game over screen.
     * <p>
     * Depending on whether the user is eliminated or wins the game, this method draws the corresponding
     * game over message and logos, as well as instructions for replaying or exiting the game.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     */
    private void drawGameOverScreen(final GraphicsContext gc)
    {
        gc.setFill(Color.BLACK);
        gc.fillRect(COORDINATE_ZERO,
                    COORDINATE_ZERO,
                    CANVAS_WIDTH_IN_PIXEL,
                    CANVAS_HEIGHT_IN_PIXEL);

        if(gameLogic.getUser().isEliminated())
        {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL));
            final String message;
            final double textWidth;
            final double centerX;
            final double centerY;

            message = "You die! Press ENTER to try again.";
            textWidth = computeTextWidth(message, gc.getFont());
            centerX = (CANVAS_WIDTH_IN_PIXEL - textWidth) / LOGO_MIDDLE_FACTOR;
            centerY = CANVAS_HEIGHT_IN_PIXEL / LOGO_MIDDLE_FACTOR;
            gc.fillText(message,
                        centerX,
                        centerY);
        }
        else
        {
            // Winning screen
            // Draw the winning logo (similar to how drawIntroScreen does it)
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(FONT_STYLE,
                                CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT));

            final int logoHeight;
            logoHeight = logoWinLines.length;

            // Center vertically and leave a little room above/below
            final double logoY;
            logoY = (double) CANVAS_HEIGHT_IN_PIXEL / LOGO_MIDDLE_FACTOR
                    - (logoHeight * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT) / LOGO_MIDDLE_FACTOR
                    - (CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT);

            for(int i = 0; i < logoWinLines.length; i++)
            {
                final String line;
                final double textWidth;
                final double x;

                line = logoWinLines[i];
                textWidth = computeTextWidth(line, gc.getFont());
                x = (CANVAS_WIDTH_IN_PIXEL - textWidth) / LOGO_MIDDLE_FACTOR;
                gc.fillText(line,
                            x,
                        logoY + i * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT);
            }

            // Now show the “You win!” message below the logo
            gc.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL));
            final String message;
            final double messageWidth;
            final double messageX;
            final double messageY;

            message = "You win! But have you truly earned it after all these fallen fates?";
            messageWidth = computeTextWidth(message, gc.getFont());
            messageX = (CANVAS_WIDTH_IN_PIXEL - messageWidth) / LOGO_MIDDLE_FACTOR;
            messageY = logoY + logoHeight * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT + CELL_SIZE_IN_PIXEL;
            gc.fillText(message,
                        messageX,
                        messageY);

            // Show replay/exit instructions
            final String instructions;
            final double instrWidth;
            final double instrX;
            final double instrY;

            instructions = "Press ENTER to Play Again or ESC to Exit";
            instrWidth = computeTextWidth(instructions, gc.getFont());
            instrX = (CANVAS_WIDTH_IN_PIXEL - instrWidth) / LOGO_MIDDLE_FACTOR;
            instrY = messageY + CELL_SIZE_IN_PIXEL * LOGO_MIDDLE_FACTOR;
            gc.fillText(instructions,
                        instrX,
                        instrY);
        }
    }

    /*
     * Draws the red light machine.
     * <p>
     * This method draws a red-colored ASCII art doll representing the red light machine at a centered position.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     */
    private void drawLightMachine(final GraphicsContext gc)
    {
        final String[] doll =
        {
                "   .^-^.     ",
                "   (o o)     ",
                "   / V \\     ",
                "  /|---|\\    ",
                "   |===|     ",
                "   \\ | /     ",
                "    \\|/     "
        };

        gc.setFill(Color.RED);

        // Compute starting position to center the doll.
        final double dollWidth;
        final double startX;
        final double startY;

        dollWidth = doll[FIRST_INDEX].length() * (CELL_SIZE_IN_PIXEL * LIGHT_MACHINE_SIZE_WEIGHT);
        startX = (CANVAS_WIDTH_IN_PIXEL - dollWidth) / LOGO_MIDDLE_FACTOR;
        startY = TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;

        // Draw each line of the doll.
        for(int i = 0; i < doll.length; i++)
        {
            putSafeString(gc,
                          startX,
                          startY + i * CELL_SIZE_IN_PIXEL,
                          doll[i]);
        }
    }

    /*
     * Draws the green light machine.
     * <p>
     * This method draws a green-colored ASCII art doll representing the green light machine at a centered position.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     */
    private void drawGreenMachine(final GraphicsContext gc)
    {
        String[] doll =
        {
                "   .^-^.     ",
                "   (   )     ",
                "   / - \\     ",
                "  /|---|\\    ",
                "   |===|     ",
                "   \\ | /     ",
                "    \\|/     "
        };

        gc.setFill(Color.GREEN);

        // Compute starting position to center the doll.
        double dollWidth = doll[FIRST_INDEX].length() * (CELL_SIZE_IN_PIXEL * LIGHT_MACHINE_SIZE_WEIGHT);
        double startX = (CANVAS_WIDTH_IN_PIXEL - dollWidth) / LOGO_MIDDLE_FACTOR;
        double startY = TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL; // same position as red light

        // Draw each line of the doll.
        for(int i = 0; i < doll.length; i++)
        {
            putSafeString(gc,
                          startX,
                          startY + i * CELL_SIZE_IN_PIXEL,
                          doll[i]);
        }
    }

    /*
     * Draws a dead player's sprite.
     * <p>
     * This method draws a representation of a dead player using gray color to indicate elimination.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     * @param p  the Player whose dead sprite is to be drawn.
     */
    private void drawDeadSprite(final GraphicsContext gc,
                                final Player p)
    {
        final double baseX;
        final double baseY;

        baseX = (p.getX() - SPRITE_OFFSET_ONE) * CELL_SIZE_IN_PIXEL;
        baseY = p.getY() * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;

        gc.setFill(Color.GRAY); // Use gray to indicate death.
        // Draw a dead body.
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                      "  ____");
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                      "--O---");
    }

    /*
     * Draws a player's sprite.
     * <p>
     * This method draws the sprite for a player. For the user-controlled player, it shows a different color
     * and may display additional visual cues if the player is pushing or being pushed.
     * </p>
     *
     * @param gc the GraphicsContext used for drawing.
     * @param p  the Player whose sprite is to be drawn.
     */
    private void drawPlayerSprite(final GraphicsContext gc,
                                  final Player p)
    {
        final double baseX;
        final double baseY;

        baseX = (p.getX() - SPRITE_OFFSET_ONE) * CELL_SIZE_IN_PIXEL;
        baseY = p.getY() * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;

        if(p.isUser())
        {
            gc.setFill(Color.CYAN);
            if(p.isPushing())
            {
                drawPushingSprite(gc,
                                  p,
                                  baseX,
                                  baseY);
            }
            else if
            (p.isPushed())
            {
                drawPushedSprite(gc,
                                 p,
                                 baseX,
                                 baseY);
            }
            else
            {
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_FOUR * CELL_SIZE_IN_PIXEL,
                              "YOU");
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                              SPRITE_HEAD);
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                              SPRITE_UPPER_BODY);
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                              SPRITE_CORE_BODY);
                putSafeString(gc,
                              baseX,
                              baseY,
                              SPRITE_LOWER_BODY);
            }
        }
        else
        {
            gc.setFill(Color.WHITE);
            // For NPCs, you might only want to show a pushed sprite if they're being pushed.
            if(p.isPushed())
            {
                drawPushedSprite(gc,
                                 p,
                                 baseX,
                                 baseY);
            }
            else
            {
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                              SPRITE_HEAD);
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                              SPRITE_UPPER_BODY);
                putSafeString(gc,
                              baseX,
                              baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                              SPRITE_CORE_BODY);
                putSafeString(gc,
                              baseX,
                              baseY,
                              SPRITE_LOWER_BODY);
            }
        }
    }

    /*
     * Draws a sprite for a player who is pushing.
     * <p>
     * This method renders a modified sprite to indicate that the player is pushing.
     * </p>
     *
     * @param gc    the GraphicsContext used for drawing.
     * @param p     the Player who is pushing.
     * @param baseX the base x-coordinate for the sprite.
     * @param baseY the base y-coordinate for the sprite.
     */
    private void drawPushingSprite(final GraphicsContext gc,
                                   final Player p,
                                   final double baseX,
                                   final double baseY)
    {
        gc.setFill(p.isUser() ? Color.CYAN : Color.WHITE);
        putSafeString(gc,
                      baseX,
                      baseY - 4 * CELL_SIZE_IN_PIXEL,
                      p.isUser() ? "YOU" : "");
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                      SPRITE_HEAD);
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                      "-|\\"); // Note the extra dash on the left to suggest pushing.
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                      SPRITE_CORE_BODY);
        putSafeString(gc,
                      baseX,
                      baseY,
                      SPRITE_LOWER_BODY);
    }

    /*
     * Draws a sprite for a player who is being pushed.
     * <p>
     * This method renders a modified sprite to indicate that the player is being pushed.
     * </p>
     *
     * @param gc    the GraphicsContext used for drawing.
     * @param p     the Player who is being pushed.
     * @param baseX the base x-coordinate for the sprite.
     * @param baseY the base y-coordinate for the sprite.
     */
    private void drawPushedSprite(final GraphicsContext gc,
                                  final Player p,
                                  final double baseX,
                                  final double baseY)
    {
        gc.setFill(p.isUser() ? Color.CYAN : Color.WHITE);
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                      SPRITE_HEAD);
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                      SPRITE_UPPER_BODY);
        putSafeString(gc,
                      baseX,
                      baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                      " |\\"); // Slightly shifted leg or extra mark to indicate imbalance.
        putSafeString(gc,
                      baseX,
                      baseY,
                      SPRITE_LOWER_BODY);
    }

    /*
     * Validates that the provided GameLogic instance is not null.
     *
     * @param gameLogic the GameLogic instance to validate.
     * @throws IllegalArgumentException if the provided gameLogic is null.
     */
    private static void validateGameLogic(final GameLogic gameLogic)
    {
        if(gameLogic == null)
        {
            throw new IllegalArgumentException("GameLogic instance cannot be null.");
        }
    }
}
