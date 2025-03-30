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
 *
 * This class draws user interface of the game.
 * This class is responsible for drawing intro screen, game over screen,
 * and graphical parts of game window like player sprite or light machine.
 * @author Kyle Cheon
 * @version 1.0
 */
public class GameRenderer
{
    private String[] logoLines;
    private String[] logoWinLines;
    private final GameLogic gameLogic;

    public GameRenderer(final GameLogic gameLogic)
    {
        this.gameLogic = gameLogic;

        try
        {
            logoLines = ResourceLoader.loadLogo();
        } catch (final IOException e) {
            logoLines = new String[]{"RED LIGHT", "BLOOD LIGHT"};
        }

        try
        {
            logoWinLines = ResourceLoader.loadWinLogo();
        } catch (final IOException e) {
            logoWinLines = new String[]{
                    "YOU WIN!",
                    "(No fancy win logo found.)"
            };
        }
    }

    public void render(GameState state, GraphicsContext gc) {
        switch(state) {
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


    private void drawIntroScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH_IN_PIXEL, CANVAS_HEIGHT_IN_PIXEL);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL * 0.75));

        int logoHeight = logoLines.length;
        double logoY = CANVAS_HEIGHT_IN_PIXEL / 2 - (logoHeight * CELL_SIZE_IN_PIXEL * 0.75) / 2 - (CELL_SIZE_IN_PIXEL * 0.75);
        for (int i = 0; i < logoLines.length; i++) {
            String line = logoLines[i];
            double textWidth = computeTextWidth(line, gc.getFont());
            double x = (CANVAS_WIDTH_IN_PIXEL - textWidth) / 2;
            gc.fillText(line, x, logoY + i * CELL_SIZE_IN_PIXEL * 0.75);
        }

        gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL)); // Back to original size for title
        String titlePart1 = "WELCOME TO RED LIGHT ";
        String titlePart2 = " LIGHT";
        String redWord = "BLOOD";
        String fullTitle = titlePart1 + redWord + titlePart2;
        double titleY = logoY + logoHeight * CELL_SIZE_IN_PIXEL * 0.75 + CELL_SIZE_IN_PIXEL;
        double titleWidth = computeTextWidth(fullTitle, gc.getFont());
        double titleX = (CANVAS_WIDTH_IN_PIXEL - titleWidth) / 2;
        gc.setFill(Color.WHITE);
        gc.fillText(titlePart1, titleX, titleY);
        double offset = computeTextWidth(titlePart1, gc.getFont());
        gc.setFill(Color.RED);
        gc.fillText(redWord, titleX + offset, titleY);
        offset += computeTextWidth(redWord, gc.getFont());
        gc.setFill(Color.WHITE);
        gc.fillText(titlePart2, titleX + offset, titleY);

        String instructions = "Press ENTER to PLAY or ESC to EXIT";
        double instrWidth = computeTextWidth(instructions, gc.getFont());
        double instrX = (CANVAS_WIDTH_IN_PIXEL - instrWidth) / 2;
        double instrY = titleY + CELL_SIZE_IN_PIXEL * 2;
        gc.fillText(instructions, instrX, instrY);
    }

    private void drawGame(GraphicsContext gc) {
        // Compute elapsed time in seconds.
        double elapsedSeconds = (System.nanoTime() - gameLogic.getGameStartTimeInNanoseconds()) / 1_000_000_000.0;

        // Count dead players.
        int deadCount = 0;
        for (Player p : gameLogic.getPlayers()) {
            if (p.isEliminated()) {
                deadCount++;
            }
        }

        // Clear previous stats
        gc.setFill(Color.BLACK);
        gc.fillRect(0, TOP_MARGIN_IN_PIXEL, CANVAS_WIDTH_IN_PIXEL, STAT_HEIGHT_IN_PIXEL);

        // Prepare the stats string.
        String stats = String.format("Time: %.1fs   Finished: %d   Dead: %d",
                elapsedSeconds, gameLogic.getFinishedCount(), deadCount);

        // Draw the stats at the top left (using a smaller font so it fits nicely).
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL * 3 / 5));
        gc.fillText(stats, 5, TOP_MARGIN_IN_PIXEL + CELL_SIZE_IN_PIXEL / 2);

        gc.setFill(Color.BLACK);
        // Clear only below the header area.
        gc.fillRect(0, TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL, CANVAS_WIDTH_IN_PIXEL, CANVAS_HEIGHT_IN_PIXEL - TOP_MARGIN_IN_PIXEL - STAT_HEIGHT_IN_PIXEL);

        gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL));
        gc.setTextAlign(TextAlignment.LEFT);
        if (!gameLogic.isGreen()) {
            // When the light is red, draw the doll machine.
            drawLightMachine(gc);
        } else {
            drawGreenMachine(gc);
        }

        double finishLineYCanvas = FINISH_LINE_Y_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;

        // Set the stroke properties.
        gc.setStroke(Color.DARKGREEN); // Choose a visible color.
        gc.setLineWidth(1);         // Adjust thickness as needed.

        // Draw a horizontal line across the canvas.
        gc.strokeLine(0, finishLineYCanvas, CANVAS_WIDTH_IN_PIXEL, finishLineYCanvas);


        long now = System.nanoTime();
        for (Player p : gameLogic.getPlayers()) {
            // Skip finished players so they no longer appear on-screen.
            if (p.isFinished()) continue;
            if (p.isEliminated()) {
                drawDeadSprite(gc, p);
            } else {
                drawPlayerSprite(gc, p);
            }
        }
    }

    private void drawGameOverScreen(GraphicsContext gc)
    {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH_IN_PIXEL, CANVAS_HEIGHT_IN_PIXEL);

        if (gameLogic.getUser().isEliminated()) {
            // Losing screen
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL));
            String message = "You die! Press ENTER to try again.";
            double textWidth = computeTextWidth(message, gc.getFont());
            double centerX = (CANVAS_WIDTH_IN_PIXEL - textWidth) / 2;
            double centerY = CANVAS_HEIGHT_IN_PIXEL / 2;
            gc.fillText(message, centerX, centerY);
        } else {
            // Winning screen

            // Draw the winning logo (similar to how drawIntroScreen does it)
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL * 0.75));

            int logoHeight = logoWinLines.length;
            // Center vertically and leave a little room above/below
            double logoY = CANVAS_HEIGHT_IN_PIXEL / 2
                    - (logoHeight * CELL_SIZE_IN_PIXEL * 0.75) / 2
                    - (CELL_SIZE_IN_PIXEL * 0.75);

            for (int i = 0; i < logoWinLines.length; i++) {
                String line = logoWinLines[i];
                double textWidth = computeTextWidth(line, gc.getFont());
                double x = (CANVAS_WIDTH_IN_PIXEL - textWidth) / 2;
                gc.fillText(line, x, logoY + i * CELL_SIZE_IN_PIXEL * 0.75);
            }

            // Now show the “You win!” message below the logo
            gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL));
            String message = "You win! But have you truly earned it "
                    + "after all these fallen fates?";
            double messageWidth = computeTextWidth(message, gc.getFont());
            double messageX = (CANVAS_WIDTH_IN_PIXEL - messageWidth) / 2;
            double messageY = logoY + logoHeight * CELL_SIZE_IN_PIXEL * 0.75 + CELL_SIZE_IN_PIXEL;
            gc.fillText(message, messageX, messageY);

            // Show replay/exit instructions
            String instructions = "Press ENTER to Play Again or ESC to Exit";
            double instrWidth = computeTextWidth(instructions, gc.getFont());
            double instrX = (CANVAS_WIDTH_IN_PIXEL - instrWidth) / 2;
            double instrY = messageY + CELL_SIZE_IN_PIXEL * 2;
            gc.fillText(instructions, instrX, instrY);
        }
    }

    private void drawLightMachine(GraphicsContext gc) {
        // Define the doll ASCII art as an array of strings.
        String[] doll = {
                "   .^-^.     ",
                "   (o o)     ",
                "   / V \\     ",
                "  /|---|\\    ",
                "   |===|     ",
                "   \\ | /     ",
                "    \\|/     "
        };

        // Set the fill color to red.
        gc.setFill(Color.RED);

        // Compute starting position to center the doll.
        // Adjust the width calculation factor as needed (here CELL_SIZE*0.6 is an approximate width per character).
        double dollWidth = doll[0].length() * (CELL_SIZE_IN_PIXEL * 0.6);
        double startX = (CANVAS_WIDTH_IN_PIXEL - dollWidth) / 2;
        double startY = TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL; // Position below the header.

        // Draw each line of the doll.
        for (int i = 0; i < doll.length; i++) {
            putSafeString(gc, startX, startY + i * CELL_SIZE_IN_PIXEL, doll[i]);
        }
    }

    private void drawGreenMachine(GraphicsContext gc) {
        String[] doll = {
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
        double dollWidth = doll[0].length() * (CELL_SIZE_IN_PIXEL * 0.6);
        double startX = (CANVAS_WIDTH_IN_PIXEL - dollWidth) / 2;
        double startY = TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL; // same position as red light

        // Draw each line of the doll.
        for (int i = 0; i < doll.length; i++) {
            putSafeString(gc, startX, startY + i * CELL_SIZE_IN_PIXEL, doll[i]);
        }
    }

    private void drawDeadSprite(GraphicsContext gc, Player p) {
        double baseX = (p.getX() - 1) * CELL_SIZE_IN_PIXEL;
        double baseY = p.getY() * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;
        gc.setFill(Color.GRAY); // Use gray to indicate death.
        // Draw a dead body.
        putSafeString(gc, baseX, baseY - 2 * CELL_SIZE_IN_PIXEL, "  ____");
        putSafeString(gc, baseX, baseY - 1 * CELL_SIZE_IN_PIXEL, "--O---");
    }

    private void drawPlayerSprite(GraphicsContext gc, Player p) {
        double baseX = (p.getX() - 1) * CELL_SIZE_IN_PIXEL;
        double baseY = p.getY() * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + STAT_HEIGHT_IN_PIXEL;
        if (p.isUser()) {
            gc.setFill(Color.CYAN);
            if (p.isPushing()) {
                drawPushingSprite(gc, p, baseX, baseY);
            } else if (p.isPushed()) {
                drawPushedSprite(gc, p, baseX, baseY);
            } else {
                putSafeString(gc, baseX, baseY - 4 * CELL_SIZE_IN_PIXEL, "YOU");
                putSafeString(gc, baseX, baseY - 3 * CELL_SIZE_IN_PIXEL, " O ");
                putSafeString(gc, baseX, baseY - 2 * CELL_SIZE_IN_PIXEL, "/|\\");
                putSafeString(gc, baseX, baseY - 1 * CELL_SIZE_IN_PIXEL, " | ");
                putSafeString(gc, baseX, baseY, "/ \\");
            }
        } else {
            gc.setFill(Color.WHITE);
            // For NPCs, you might only want to show a pushed sprite if they're being pushed.
            if (p.isPushed()) {
                drawPushedSprite(gc, p, baseX, baseY);
            } else {
                putSafeString(gc, baseX, baseY - 3 * CELL_SIZE_IN_PIXEL, " O ");
                putSafeString(gc, baseX, baseY - 2 * CELL_SIZE_IN_PIXEL, "/|\\");
                putSafeString(gc, baseX, baseY - 1 * CELL_SIZE_IN_PIXEL, " | ");
                putSafeString(gc, baseX, baseY, "/ \\");
            }
        }
    }


    private void drawPushingSprite(GraphicsContext gc, Player p, double baseX, double baseY) {
        // Example ASCII art for the pushing action.
        // You can adjust these strings as needed.
        gc.setFill(p.isUser() ? Color.CYAN : Color.WHITE);
        putSafeString(gc, baseX, baseY - 4 * CELL_SIZE_IN_PIXEL, p.isUser() ? "YOU" : "");
        putSafeString(gc, baseX, baseY - 3 * CELL_SIZE_IN_PIXEL, " O ");
        // Note the extra dash on the left to suggest pushing.
        putSafeString(gc, baseX, baseY - 2 * CELL_SIZE_IN_PIXEL, "-|\\");
        putSafeString(gc, baseX, baseY - 1 * CELL_SIZE_IN_PIXEL, " | ");
        putSafeString(gc, baseX, baseY, "/ \\");
    }

    private void drawPushedSprite(GraphicsContext gc, Player p, double baseX, double baseY) {
        // Example ASCII art for a pushed/losing-balance sprite.
        gc.setFill(p.isUser() ? Color.CYAN : Color.WHITE);
        putSafeString(gc, baseX, baseY - 3 * CELL_SIZE_IN_PIXEL, " O ");
        putSafeString(gc, baseX, baseY - 2 * CELL_SIZE_IN_PIXEL, "/|\\");
        // Slightly shifted leg or extra mark to indicate imbalance.
        putSafeString(gc, baseX, baseY - 1 * CELL_SIZE_IN_PIXEL, " |\\");
        putSafeString(gc, baseX, baseY, "/ \\");
    }
}
