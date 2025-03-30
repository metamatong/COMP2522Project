# Java Project: WordGame, NumberGame, and MyGame

This repository contains three Java-based games—**WordGame**, **NumberGame**, and **MyGame**—demonstrating a range of skills and best practices learned throughout the course. The project uses both terminal-based (WordGame, MyGame if desired) and GUI-based (NumberGame) approaches, showcases file handling, data structures, OOP principles, and more.

---

## Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Games Description](#games-description)
    - [WordGame](#wordgame)
    - [NumberGame](#numbergame)
    - [MyGame](#mygame)
4. [Score Tracking](#score-tracking)
5. [Running the Project](#running-the-project)
6. [Submission Requirements](#submission-requirements)
7. [Scoring Rubric Summary](#scoring-rubric-summary)
8. [Video Requirements](#video-requirements)

---

## Overview

The goal of this project is to create three distinct games showcasing proper Java coding techniques, data structures, OOP design, file I/O, and user interaction via both console menus and a graphical user interface (GUI). The three games are:

- **WordGame**: A text-based geography trivia javaFXGame (played in the terminal).
- **NumberGame**: A GUI-based puzzle where players must place generated numbers in ascending order.
- **MyGame**: A unique, original javaFXGame idea created with the help of an AI assistant—while still following the coding patterns and style guidelines learned in this course.

The user navigates between these games via the `Main` class, which presents a menu in an infinite loop until the user decides to quit.

---

## Project Structure

**Required Classes**
1. **Main.java**
    - Displays a main menu in the console:
        - **W** or **w** → Launches **WordGame**
        - **N** or **n** → Launches **NumberGame**
        - **M** or **m** → Launches **MyGame**
        - **Q** or **q** → Quits the program
    - Validates user input and re-prompts for invalid entries.

2. **WordGame.java**
    - Implements a 10-question quiz about countries, capitals, and facts.
    - Draws data from text files (`a.txt`, `b.txt`, …, `z.txt`) which you will load into a `World` class (with a `HashMap<String, Country>`).
    - Tracks scoring and allows the user to replay multiple times.
    - Appends results to `score.txt` on exit and checks/updates the high-score record.

3. **NumberGame.java**
    - Provides a 4×5 grid in a GUI.
    - Randomly generates 20 integers from 1–1000.
    - Users must place each number into an empty cell, maintaining ascending order or face a javaFXGame-over.
    - Tracks wins/losses, average placements, and allows replays.

4. **MyGame.java**
    - A unique javaFXGame demonstrating concepts from the entire course.
    - Must include a `prompts.txt` describing how AI was used and an `applications.txt` indicating where course concepts appear.
    - Must maintain consistent style, follow best practices, and be distinct from publicly-available code.

5. **Country.java**
    - Holds data for each country:
        - `String name`
        - `String capitalCityName`
        - `String[] facts` (with exactly three elements)

6. **Score.java**
    - Stores data for WordGame’s scoring summary:
        - `LocalDateTime dateTimePlayed`
        - `int numGamesPlayed`
        - `int numCorrectFirstAttempt`
        - `int numCorrectSecondAttempt`
        - `int numIncorrectTwoAttempts`
    - Writes to/reads from `score.txt` and helps identify high score (points per javaFXGame).

7. **Additional Interfaces & Classes**
    - For **NumberGame**, at least one interface, one abstract class, and one concrete class are required.
    - Any other helper classes as needed by your design.

---

## Games Description

### WordGame

A console-based trivia quiz about world geography. It randomly generates 10 questions, each of which can be one of three types:
1. Given a **capital city**, name its **country**.
2. Given a **country**, name its **capital city**.
3. Given one **fact**, name the **country** to which that fact belongs.

**Key Points:**
- Users have **two attempts** per question:
    - Correct on the first try earns **2 points**.
    - Correct on the second try earns **1 point**.
    - Wrong on both attempts yields **0 points**.
- After 10 questions, display:
    - Total games played (in the current session)
    - Correct answers on the first attempt
    - Correct answers on the second attempt
    - Incorrect answers (two failed attempts)
- Offer to **Play Again** or **Quit**:
    - If "Yes" (any capitalization), reset for another javaFXGame.
    - If "No" (any capitalization), exit to the main menu.
    - Invalid input re-prompts the user.
- Upon exiting WordGame:
    - Results are appended to `score.txt`.
    - Check for a “high score” (highest average points per javaFXGame):
        - If new record: Congratulate the user and show the old record’s details.
        - Otherwise: Display the existing high score record.

### NumberGame

A GUI-based puzzle where 20 random numbers (1–1000) are drawn one at a time, and the player places each number into a **4×5 grid**:

1. **Ascension Requirement**:
    - Numbers must be placed such that the final arrangement is in ascending order from the first to the last cell.
2. **Game Over Conditions**:
    - If a new random number cannot be placed in correct ascending order, the user immediately loses.
3. **Tracking & Display**:
    - Track:
        - Total games played
        - Wins and losses
        - Number of successful placements
        - Average placements per javaFXGame
    - At javaFXGame end, offer **Try Again** or **Quit**:
        - **Try Again** → Resets the grid and draws new random numbers.
        - **Quit** → Returns to the main menu and displays summary stats.

### MyGame

A **unique and personal** javaFXGame you design in Java. Must:
- Use concepts from each lesson of the course, as documented in `applications.txt`.
- Involve an **AI prompt** workflow, documented in `prompts.txt`.
- Maintain consistent style with the rest of the project.

Examples of potential javaFXGame ideas:
- A textual puzzle combining elements of Sudoku and Wordle.
- A side-scrolling or turn-based logic puzzle (console-based or minimal GUI is allowed).

---

## Score Tracking

For the **WordGame**, all javaFXGame attempts and results are stored in a text file named `score.txt`. The **Score** class manages the following:

- **Date and time of the javaFXGame session** (using `LocalDateTime` and `DateTimeFormatter`).
- **Number of games played** during the session.
- **Number of correct first-attempt answers**.
- **Number of correct second-attempt answers**.
- **Number of completely incorrect answers**.

The final average is computed as: