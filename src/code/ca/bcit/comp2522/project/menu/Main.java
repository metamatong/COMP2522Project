package ca.bcit.comp2522.project.menu;

import ca.bcit.comp2522.project.numbergame.NumberGame;
import ca.bcit.comp2522.project.wordgame.WordGame;
import ca.bcit.comp2522.project.mygame.MyGame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * The {@code Main} class serves as the entry point for the application and provides a
 * text-based menu interface from which users can select among three different games:
 * WordGame, NumberGame, and MyGame (Red Light Blood Light). This class leverages JavaFX for
 * concurrency and thread management while running the game selections on a separate console-based thread.
 * <p>
 * The class extends {@link Application} from JavaFX and overrides the {@code start} method to configure
 * the application behavior and to prevent the JavaFX runtime from automatically exiting when a stage is closed.
 * It uses the {@code Platform.runLater} method to ensure that game-related tasks are executed on the JavaFX
 * Application Thread while the console menu interaction is handled in a dedicated thread.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class Main extends Application
{
    private static final String WORD_GAME_INITIAL = "W";
    private static final String NUMBER_GAME_INITIAL = "N";
    private static final String MY_GAME_INITIAL = "M";
    private static final String QUIT_INITIAL = "Q";
    private static final int LATCH_COUNT_DOWN = 1;


    /**
     * The main entry point for the application.
     * <p>
     * This method is responsible for launching the JavaFX runtime which in turn invokes the overridden
     * {@code start} method. Command-line arguments are passed to the JavaFX framework as needed.
     * </p>
     *
     * @param args an array of command-line arguments passed to the application
     */
    public static void main(final String[] args)
    {
        // Single launch for the entire application.
        Application.launch(args);
    }

    /**
     * Initializes the primary stage and sets up the application environment.
     * <p>
     * This method configures the JavaFX environment so that the application does not exit when a stage is closed,
     * ensuring that the console-based main menu continues running. It creates and starts a new thread dedicated to
     * processing the main menu logic, which enables asynchronous execution of non-GUI tasks alongside the JavaFX
     * Application Thread.
     * </p>
     *
     * @param primaryStage the primary {@link Stage} for this application.
     */
    @Override
    public void start(final Stage primaryStage)
    {
        // Prevent the application from exiting when a stage is closed.
        Platform.setImplicitExit(false);

        // Launch the main menu logic on a new thread. This will ensure overall
        // latch design possible by making this thread wait for JavaFX Application Thread
        // to end its job.
        final Thread menuThread;

        menuThread = new Thread(this::displayMainMenu);

        menuThread.start();
    }

    /*
     * Displays the main menu in the console and processes user input in a continuous loop.
     * <p>
     * The menu provides options to launch one of three games or to exit the application:
     * <ul>
     *   <li>{@code "W"}: Launches the Word game.</li>
     *   <li>{@code "N"}: Launches the Number game (a game that uses the JavaFX thread via {@code launchGame}).</li>
     *   <li>{@code "M"}: Launches the MyGame (Red Light Blood Light) using the JavaFX thread.</li>
     *   <li>{@code "Q"}: Quits the application.</li>
     * </ul>
     * After a game finishes executing, the method prints a message confirming return from the game and re-displays
     * the menu.
     * </p>
     */
    private void displayMainMenu()
    {
        final Scanner scanner;
        scanner = new Scanner(System.in);

        while(true)
        {
            System.out.println("\nMenu:");
            System.out.println("Press " + WORD_GAME_INITIAL + " to play the Word game.");
            System.out.println("Press " + NUMBER_GAME_INITIAL + " to play the Number game.");
            System.out.println("Press " + MY_GAME_INITIAL + " to play the Red Light Blood Light.");
            System.out.println("Press " + QUIT_INITIAL + " to quit.");
            System.out.print("Enter your choice: ");

            final String input;
            input = scanner.nextLine().trim();

            if(input.equalsIgnoreCase(QUIT_INITIAL))
            {
                System.out.println("Goodbye!");
                Platform.exit();
                break;
            }
            else if(input.equalsIgnoreCase(WORD_GAME_INITIAL))
            {
                final WordGame wordGame;
                wordGame = new WordGame();
                wordGame.playWordGame();
                System.out.println("\n=== Returned from WordGame ===");
            }
            else if(input.equalsIgnoreCase(NUMBER_GAME_INITIAL))
            {
                launchGame(new NumberGame());
                System.out.println("\n=== Returned from NumberGame ===");
            }
            else if(input.equalsIgnoreCase(MY_GAME_INITIAL))
            {
                launchGame(new MyGame());
                System.out.println("\n=== Returned from MyGame ===");
            }
            else
            {
                System.out.println("Wrong input. Please try again.");
            }
        }
        scanner.close();
    }

    /*
     * Launches a JavaFX-based game and waits for its completion before returning to the main menu.
     * <p>
     * The method accepts any game that implements the {@code JavaFXGame} interface. The game is expected to
     * implement a {@code play(CountDownLatch latch)} method. This method schedules the game’s execution on
     * the JavaFX Application Thread using {@link Platform#runLater}, ensuring thread-safety for GUI operations.
     * A {@link CountDownLatch} is used to pause the main menu thread until the game signals that it has finished.
     * </p>
     *
     * @param javaFXGame an instance of a game that implements the {@code JavaFXGame} interface and encapsulates
     *                   JavaFX-based gameplay logic
     * @throws RuntimeException if the waiting thread is interrupted while waiting for the game to finish
     */
    private void launchGame(final JavaFXGame javaFXGame)
    {
        // Create a latch that waits for the game to finish.
        final CountDownLatch gameLatch;
        gameLatch = new CountDownLatch(LATCH_COUNT_DOWN);

        // Schedule the game’s play method on the JavaFX Application Thread.
        Platform.runLater(() -> javaFXGame.play(gameLatch));

        try
        {
            // Wait until the game calls countDown() method.
            gameLatch.await();
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
