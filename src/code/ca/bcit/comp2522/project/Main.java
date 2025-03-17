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
    public static void main(final String[] args)
    {
        final Scanner scanner;
        scanner = new Scanner(System.in);

        while(true)
        {
            System.out.println("\nMenu:");
            System.out.println("Press W to play the Word game.");
            System.out.println("Press N to play the Number game.");
            System.out.println("Press M to play the <your game's name> game.");
            System.out.println("Press Q to quit.");
            System.out.print("Enter your choice: ");

            final String input;
            input = scanner.nextLine().trim();

            if(input.equalsIgnoreCase("q"))
            {
                System.out.println("Goodbye!");
                break;
            }
            else if(input.equalsIgnoreCase("w"))
            {
                final WordGame wordGame;
                wordGame = new WordGame();
                wordGame.playWordGame();
            }
            else if(input.equalsIgnoreCase("n"))
            {
                NumberGame.main(new String[]{});
            }
            else if(input.equalsIgnoreCase("m"))
            {
                System.out.println("<your game's name> game is not implemented yet.");
            }
            else
            {
                System.out.println("Wrong input. Please try again.");
            }
        }

        scanner.close();
    }
}
