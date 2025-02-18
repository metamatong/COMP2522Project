package ca.bcit.comp2522.project;

/**
 * Represents the different types of word games available in the project.
 *
 * <p>This enum is used to define the types of word games offered in the application.
 * The game types include:
 * </p>
 * {@code CAPITAL_TO_COUNTRY}: The game where the program prints a capital city and the user must guess the country.</li>
 * {@code COUNTRY_TO_CAPITAL}: The game where the program prints a country name and the user must guess its capital city.</li>
 * {@code FACT_TO_COUNTRY}: The game where the program prints a fact about a country and the user must identify the country.</li>
 */
public enum WordGameType
{
    CAPITAL_TO_COUNTRY,
    COUNTRY_TO_CAPITAL,
    FACT_TO_COUNTRY
}
