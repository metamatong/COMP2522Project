package ca.bcit.comp2522.project.mygame.util;

import ca.bcit.comp2522.project.mygame.common.MovementDirection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static ca.bcit.comp2522.project.mygame.common.GameConfig.CANVAS_HEIGHT_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CANVAS_WIDTH_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CELL_SIZE_IN_PIXEL;

public class DrawingUtils
{
    public static double computeTextWidth(String text, Font font) {
        Text tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getLayoutBounds().getWidth();
    }

    // Draws a string if within canvas bounds.
    public static void putSafeString(GraphicsContext gc, double x, double y, String s) {
        if (x + s.length() * CELL_SIZE_IN_PIXEL / 2 < 0 || x > CANVAS_WIDTH_IN_PIXEL) return;
        if (y < 0 || y > CANVAS_HEIGHT_IN_PIXEL) return;
        gc.fillText(s, x, y);
    }


    // Helper: convert a Direction into an x delta.
    public static int directionDeltaX(MovementDirection d) {
        switch (d) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    // Helper: convert a Direction into a y delta.
    public static int directionDeltaY(MovementDirection d) {
        switch (d) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }
}
