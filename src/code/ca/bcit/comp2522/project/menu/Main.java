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
 * A class that drives users to play three games presented by this project.
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
     * The main entry point of the application.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args)
    {
        // Single launch for the entire application.
        Application.launch(args);
    }

    /**
     * Initializes the JavaFX application.
     * <p>
     * This method sets the application to not exit when stages are
     * closed and launches the main menu logic on a new thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which the application scene can be set.
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
     * Displays the main menu in the console and handles user input.
     * <p>
     * This method continuously presents the menu until the user chooses to quit.
     * Based on the user's choice, it either launches a game or exits the application.
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
     * Launches a game that uses JavaFX. The game must implement the Game interface,
     * meaning it should provide a play(CountDownLatch latch) method that counts down the latch when finished.
     * This design is necessary to ensure that main menu runs again after exiting each game that runs on JavaFX
     * Application Thread.
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
