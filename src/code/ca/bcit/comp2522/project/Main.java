package ca.bcit.comp2522.project;

import java.util.Scanner;

/**
 * A class that drives users to play three games presented by this project.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
class Main
{
    private static final String WORD_GAME_INITIAL = "W";
    private static final String NUMBER_GAME_INITIAL = "N";
    private static final String MY_GAME_INITIAL = "M";
    private static final String QUIT_INITIAL = "Q";

    public static void main(final String[] args)
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
                break;
            }
            else if(input.equalsIgnoreCase(WORD_GAME_INITIAL))
            {
                final WordGame wordGame;
                wordGame = new WordGame();
                wordGame.playWordGame();
            }
            else if(input.equalsIgnoreCase(NUMBER_GAME_INITIAL))
            {
                NumberGame.main(new String[]{});
            }
            else if(input.equalsIgnoreCase(MY_GAME_INITIAL))
            {
                MyGame.main(new String[]{});
            }
            else
            {
                System.out.println("Wrong input. Please try again.");
            }
        }

        scanner.close();
    }
}
