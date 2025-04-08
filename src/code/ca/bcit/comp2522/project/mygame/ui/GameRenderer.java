package ca.bcit.comp2522.project.mygame.ui;

import ca.bcit.comp2522.project.mygame.common.GameState;
import ca.bcit.comp2522.project.mygame.engine.GameLogic;
import ca.bcit.comp2522.project.mygame.entities.Player;
import ca.bcit.comp2522.project.mygame.util.DrawingUtils;
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
 * The {@code GameRenderer} class is responsible for drawing the entire user interface (UI) of the game.
 * <p>
 * It renders a variety of screens depending on the current game state, including:
 * <ul>
 *   <li><b>Intro Screen:</b> Shows the game logo, title, and instructions for starting or exiting the game.</li>
 *   <li><b>In-Game Screen:</b> Displays real-time game statistics, player sprites, the finish line, and visual
 *       representations of the current light state (green or red), along with a light machine illustration.</li>
 *   <li><b>Game Over Screen:</b> Presents a game over message that adapts based on whether the user is eliminated
 *       or has won, and provides instructions for restarting or exiting the game.</li>
 * </ul>
 * To achieve this, the class uses a JavaFX {@link GraphicsContext} for low-level rendering operations and relies on a
 * {@link GameLogic} instance to obtain current game state information. Additionally, helper methods from
 * {@code DrawingUtils} are employed to load asset resources (like logo text files) and perform safe drawing of ASCII
 * art and text strings.
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
    private final GameLogic<Player> gameLogic;

    /**
     * Constructs a new {@code GameRenderer} with the specified {@link GameLogic} instance.
     * <p>
     * The constructor initializes the renderer by validating and storing the provided {@code GameLogic} reference.
     * It then attempts to load logo assets for both the introductory and winning screens via the utility method
     * {@link DrawingUtils#loadResource(String)}. If the logo assets cannot be loaded (e.g., due to an
     * {@link IOException}), the constructor falls back to default string arrays, ensuring that the UI can still
     * display meaningful visuals.
     * </p>
     *
     * @param gameLogic the {@link GameLogic} instance used to retrieve game state information and drive UI updates.
     * @throws IllegalArgumentException if {@code gameLogic} is {@code null}.
     */
    public GameRenderer(final GameLogic<Player> gameLogic)
    {
        validateGameLogic(gameLogic);
        this.gameLogic = gameLogic;

        try
        {
            logoLines = DrawingUtils.loadResource("/logo.txt");
        }
        catch
        (final IOException e)
        {
            logoLines = new String[]{"RED LIGHT", "BLOOD LIGHT"};
        }

        try
        {
            logoWinLines = DrawingUtils.loadResource("/winLogo.txt");
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
     * Renders the game screen based on the current {@link GameState}.
     * <p>
     * This method acts as a controller that selects which screen to render by delegating the drawing operation
     * to one of the specialized methods: {@link #drawIntroScreen(GraphicsContext)} for the introductory screen,
     * {@link #drawGame(GraphicsContext)} for the in-game display, or {@link #drawGameOverScreen(GraphicsContext)} for
     * the game over screen.
     * </p>
     *
     * @param state the current game state.
     * @param graphicsContext    the {@link GraphicsContext} used for performing drawing operations.
     */
    public void render(final GameState state,
                       final GraphicsContext graphicsContext)
    {
        switch(state)
        {
            case INTRO:
                drawIntroScreen(graphicsContext);
                break;
            case GAME:
                drawGame(graphicsContext);
                break;
            case GAME_OVER:
                drawGameOverScreen(graphicsContext);
                break;
        }
    }

    /*
     * Draws the introductory screen.
     * <p>
     * This method clears the entire canvas and draws the introductory view, which includes:
     * <ul>
     *   <li>The ASCII art logo loaded from resources (or fallback content) rendered using a scaled font.</li>
     *   <li>A composed title combining multiple colored text segments to emphasize the game's theme.</li>
     *   <li>User instructions for starting or exiting the game.</li>
     * </ul>
     * </p>
     *
     * @param graphicsContext the {@link GraphicsContext} to use for drawing.
     */
    private void drawIntroScreen(final GraphicsContext graphicsContext)
    {
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(COORDINATE_ZERO,
                    COORDINATE_ZERO,
                    CANVAS_WIDTH_IN_PIXEL,
                    CANVAS_HEIGHT_IN_PIXEL);
        graphicsContext.setFill(Color.WHITE);

        // Draw intro ASCII logo image.
        graphicsContext.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT));

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
            textWidth = computeTextWidth(line, graphicsContext.getFont());
            x = (CANVAS_WIDTH_IN_PIXEL - textWidth) / LOGO_MIDDLE_FACTOR;
            graphicsContext.fillText(line,
                        x,
                    logoY + i * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT);
        }

        // Construct title sentence.
        graphicsContext.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL)); // Back to original font size for title
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
        titleWidth = computeTextWidth(fullTitle, graphicsContext.getFont());
        titleX = (CANVAS_WIDTH_IN_PIXEL - titleWidth) / LOGO_MIDDLE_FACTOR;

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillText(titlePart1,
                    titleX,
                    titleY);
        offset = computeTextWidth(titlePart1, graphicsContext.getFont());
        graphicsContext.setFill(Color.RED);
        graphicsContext.fillText(redWord,
                    titleX + offset,
                    titleY);
        offset += computeTextWidth(redWord, graphicsContext.getFont());
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillText(titlePart2,
                    titleX + offset,
                    titleY);

        // Draw instruction sentence for the user.
        final String instructions;
        final double instrWidth;
        final double instrX;
        final double instrY;

        instructions = "Press ENTER to PLAY or ESC to EXIT";
        instrWidth = computeTextWidth(instructions, graphicsContext.getFont());
        instrX = (CANVAS_WIDTH_IN_PIXEL - instrWidth) / LOGO_MIDDLE_FACTOR;
        instrY = titleY + CELL_SIZE_IN_PIXEL * LOGO_MIDDLE_FACTOR;
        graphicsContext.fillText(instructions, instrX, instrY);
    }

    /*
     * Draws the in-game screen with statistics, the light machine, finish line, and player sprites.
     * <p>
     * This method computes real-time statistics (elapsed time, finished count, dead count),
     * clears relevant canvas areas, and renders the game field. It determines the current light state
     * to draw either a red or green light machine, draws the finish line across the canvas, and iterates through
     * all players to display their active or eliminated sprites.
     * </p>
     *
     * @param graphicsContext the {@link GraphicsContext} used for drawing the in-game screen.
     */
    private void drawGame(final GraphicsContext graphicsContext)
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
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(COORDINATE_ZERO,
                    TOP_MARGIN_IN_PIXEL,
                    CANVAS_WIDTH_IN_PIXEL,
                    STAT_HEIGHT_IN_PIXEL);

        // Prepare the stats string.
        final String stats;
        stats = String.format("Time: %.1fs   Finished: %d   Dead: %d",
                elapsedSeconds, gameLogic.getFinishedCount(), deadCount);

        // Draw the stats at the top left (using a smaller font so it fits nicely).
        graphicsContext.setFill(Color.YELLOW);
        graphicsContext.setFont(Font.font(FONT_STYLE,
                CELL_SIZE_IN_PIXEL * GAME_STAT_CHARACTER_SIZE_WEIGHT));
        graphicsContext.fillText(stats,
                GAME_STATS_OFFSET_IN_NUMBER_OF_CELLS,
                TOP_MARGIN_IN_PIXEL + CELL_SIZE_IN_PIXEL / LOGO_MIDDLE_FACTOR);

        graphicsContext.setFill(Color.BLACK);
        // Clear only below the header area.
        graphicsContext.fillRect(COORDINATE_ZERO,
                TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL,
                CANVAS_WIDTH_IN_PIXEL,
                CANVAS_HEIGHT_IN_PIXEL - TOP_MARGIN_IN_PIXEL - STAT_HEIGHT_IN_PIXEL);

        graphicsContext.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL));
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        if(!gameLogic.isGreen())
        {
            drawLightMachine(graphicsContext);
        }
        else
        {
            drawGreenMachine(graphicsContext);
        }

        double finishLineYCanvas = FINISH_LINE_Y_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL +
                                   TOP_MARGIN_IN_PIXEL +
                                   STAT_HEIGHT_IN_PIXEL;

        // Set the stroke properties.
        graphicsContext.setStroke(Color.DARKGREEN);
        graphicsContext.setLineWidth(FINISH_LINE_WIDTH_IN_PIXEL);

        // Draw a horizontal line across the canvas.
        graphicsContext.strokeLine(COORDINATE_ZERO,
                      finishLineYCanvas,
                      CANVAS_WIDTH_IN_PIXEL,
                      finishLineYCanvas);

        for(final Player p : gameLogic.getPlayers())
        {
            // Skip finished players so they no longer appear on-screen.
            if (p.isFinished()) continue;
            if (p.isEliminated())
            {
                drawDeadSprite(graphicsContext, p);
            }
            else
            {
                drawPlayerSprite(graphicsContext, p);
            }
        }
    }

    /*
     * Draws the game over screen.
     * <p>
     * This method clears the canvas and then draws an appropriate game over message based on whether the user is
     * eliminated. For an eliminated user, a simple text message is shown. If the user wins, the method displays a
     * winning logo (loaded from resources or defaulted) and additional win messages, along with instructions for
     * replaying or exiting the game.
     * </p>
     *
     * @param graphicsContext the {@link GraphicsContext} used for drawing the game over screen.
     */
    private void drawGameOverScreen(final GraphicsContext graphicsContext)
    {
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(COORDINATE_ZERO,
                    COORDINATE_ZERO,
                    CANVAS_WIDTH_IN_PIXEL,
                    CANVAS_HEIGHT_IN_PIXEL);

        if(gameLogic.getUser().isEliminated())
        {
            graphicsContext.setFill(Color.WHITE);
            graphicsContext.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL));
            final String message;
            final double textWidth;
            final double centerX;
            final double centerY;

            message = "You die! Press ENTER to try again.";
            textWidth = computeTextWidth(message, graphicsContext.getFont());
            centerX = (CANVAS_WIDTH_IN_PIXEL - textWidth) / LOGO_MIDDLE_FACTOR;
            centerY = CANVAS_HEIGHT_IN_PIXEL / LOGO_MIDDLE_FACTOR;
            graphicsContext.fillText(message,
                        centerX,
                        centerY);
        }
        else
        {
            // Winning screen
            // Draw the winning logo (similar to how drawIntroScreen does it)
            graphicsContext.setFill(Color.WHITE);
            graphicsContext.setFont(Font.font(FONT_STYLE,
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
                textWidth = computeTextWidth(line, graphicsContext.getFont());
                x = (CANVAS_WIDTH_IN_PIXEL - textWidth) / LOGO_MIDDLE_FACTOR;
                graphicsContext.fillText(line,
                            x,
                        logoY + i * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT);
            }

            // Now show the “You win!” message below the logo
            graphicsContext.setFont(Font.font(FONT_STYLE, CELL_SIZE_IN_PIXEL));
            final String message;
            final double messageWidth;
            final double messageX;
            final double messageY;

            message = "You win! But have you truly earned it after all these fallen fates?";
            messageWidth = computeTextWidth(message, graphicsContext.getFont());
            messageX = (CANVAS_WIDTH_IN_PIXEL - messageWidth) / LOGO_MIDDLE_FACTOR;
            messageY = logoY + logoHeight * CELL_SIZE_IN_PIXEL * INTRO_LOGO_CHARACTER_SIZE_WEIGHT + CELL_SIZE_IN_PIXEL;
            graphicsContext.fillText(message,
                        messageX,
                        messageY);

            // Show replay/exit instructions
            final String instructions;
            final double instrWidth;
            final double instrX;
            final double instrY;

            instructions = "Press ENTER to Play Again or ESC to Exit";
            instrWidth = computeTextWidth(instructions, graphicsContext.getFont());
            instrX = (CANVAS_WIDTH_IN_PIXEL - instrWidth) / LOGO_MIDDLE_FACTOR;
            instrY = messageY + CELL_SIZE_IN_PIXEL * LOGO_MIDDLE_FACTOR;
            graphicsContext.fillText(instructions,
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
     * @param graphicsContext the GraphicsContext used for drawing.
     */
    private void drawLightMachine(final GraphicsContext graphicsContext)
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

        graphicsContext.setFill(Color.RED);

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
            putSafeString(graphicsContext,
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
     * @param graphicsContext the GraphicsContext used for drawing.
     */
    private void drawGreenMachine(final GraphicsContext graphicsContext)
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

        graphicsContext.setFill(Color.GREEN);

        // Compute starting position to center the doll.
        double dollWidth = doll[FIRST_INDEX].length() * (CELL_SIZE_IN_PIXEL * LIGHT_MACHINE_SIZE_WEIGHT);
        double startX = (CANVAS_WIDTH_IN_PIXEL - dollWidth) / LOGO_MIDDLE_FACTOR;
        double startY = TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL; // same position as red light

        // Draw each line of the doll.
        for(int i = 0; i < doll.length; i++)
        {
            putSafeString(graphicsContext,
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
     * @param graphicsContext the GraphicsContext used for drawing.
     * @param player  the Player whose dead sprite is to be drawn.
     */
    private void drawDeadSprite(final GraphicsContext graphicsContext,
                                final Player player)
    {
        final double baseX;
        final double baseY;

        baseX = (player.getX() - SPRITE_OFFSET_ONE) * CELL_SIZE_IN_PIXEL;
        baseY = player.getY() * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;

        graphicsContext.setFill(Color.GRAY); // Use gray to indicate death.
        // Draw a dead body.
        putSafeString(graphicsContext,
                      baseX,
                      baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                      "  ____");
        putSafeString(graphicsContext,
                      baseX,
                      baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                      "--O---");
    }

    /*
     * Draws a live player's sprite.
     * <p>
     * This method renders the sprite for a player by choosing different visual representations based on the player's
     * state. If the player is controlled by the user and is in a pushing or being pushed state, corresponding special
     * rendering is performed. Otherwise, a standard multi-line ASCII sprite is drawn.
     * </p>
     *
     * @param graphicsContext the {@link GraphicsContext} used for drawing.
     * @param player  the {@link Player} whose sprite is to be rendered.
     */
    private void drawPlayerSprite(final GraphicsContext graphicsContext,
                                  final Player player)
    {
        final double baseX;
        final double baseY;

        baseX = (player.getX() - SPRITE_OFFSET_ONE) * CELL_SIZE_IN_PIXEL;
        baseY = player.getY() * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;

        if(player.isUser())
        {
            graphicsContext.setFill(Color.CYAN);
            if(player.isPushing())
            {
                drawPushingSprite(graphicsContext,
                                  player,
                                  baseX,
                                  baseY);
            }
            else if
            (player.isPushed())
            {
                drawPushedSprite(graphicsContext,
                                 player,
                                 baseX,
                                 baseY);
            }
            else
            {
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_FOUR * CELL_SIZE_IN_PIXEL,
                              "YOU");
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                              SPRITE_HEAD);
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                              SPRITE_UPPER_BODY);
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                              SPRITE_CORE_BODY);
                putSafeString(graphicsContext,
                              baseX,
                              baseY,
                              SPRITE_LOWER_BODY);
            }
        }
        else
        {
            graphicsContext.setFill(Color.WHITE);
            // For NPCs, you might only want to show a pushed sprite if they're being pushed.
            if(player.isPushed())
            {
                drawPushedSprite(graphicsContext,
                                 player,
                                 baseX,
                                 baseY);
            }
            else
            {
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                              SPRITE_HEAD);
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                              SPRITE_UPPER_BODY);
                putSafeString(graphicsContext,
                              baseX,
                              baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                              SPRITE_CORE_BODY);
                putSafeString(graphicsContext,
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
     * @param graphicsContext    the GraphicsContext used for drawing.
     * @param player     the Player who is pushing.
     * @param baseX the base x-coordinate for the sprite.
     * @param baseY the base y-coordinate for the sprite.
     */
    private void drawPushingSprite(final GraphicsContext graphicsContext,
                                   final Player player,
                                   final double baseX,
                                   final double baseY)
    {
        graphicsContext.setFill(player.isUser() ? Color.CYAN : Color.WHITE);
        putSafeString(graphicsContext,
                      baseX,
                      baseY - 4 * CELL_SIZE_IN_PIXEL,
                      player.isUser() ? "YOU" : "");
        putSafeString(graphicsContext,
                      baseX,
                      baseY - SPRITE_OFFSET_THREE * CELL_SIZE_IN_PIXEL,
                      SPRITE_HEAD);
        putSafeString(graphicsContext,
                      baseX,
                      baseY - SPRITE_OFFSET_TWO * CELL_SIZE_IN_PIXEL,
                      "-|\\"); // Note the extra dash on the left to suggest pushing.
        putSafeString(graphicsContext,
                      baseX,
                      baseY - SPRITE_OFFSET_ONE * CELL_SIZE_IN_PIXEL,
                      SPRITE_CORE_BODY);
        putSafeString(graphicsContext,
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
