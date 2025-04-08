package ca.bcit.comp2522.project.wordgame;

/**
 * Represents country-specific information for use in the word game.
 * <p>
 * This class encapsulates the details of a country, including its name, the name of its capital city,
 * and an array of interesting facts about the country. Instances of this class are intended to be used as
 * data objects for game questions, trivia, or other country-related content.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
class Country
{
    private static final int ZERO_VALUE = 0;

    private final String name;
    private final String capitalCityName;
    private final String[] facts;

    /**
     * Constructs a {@code Country} object with the specified name, capital city, and facts.
     * <p>
     * The constructor validates each parameter to ensure that the country name, capital city name, and facts
     * array are neither {@code null} nor empty. If any validation fails, an {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param name             the name of the country; must not be null or empty.
     * @param capitalCityName  the name of the capital city; must not be null or empty.
     * @param facts            an array of interesting facts about the country; must not be null or empty.
     * @throws IllegalArgumentException if any input parameter is null or empty.
     */
    Country(final String name,
            final String capitalCityName,
            final String[] facts)
    {
        validateNames(name);
        validateNames(capitalCityName);
        validateFacts(facts);

        this.name = name;
        this.capitalCityName = capitalCityName;
        this.facts = facts;
    }

    /**
     * Returns the name of the country.
     *
     * @return the country name.
     */
    public String getCountryName()
    {
        return name;
    }

    /**
     * Returns the name of the capital city.
     *
     * @return the capital city name.
     */
    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    /**
     * Returns an array of facts about the country.
     *
     * @return the array of facts.
     */
    public String[] getFactsArray()
    {
        return facts;
    }

    /*
     * Validates that the provided name is neither null nor empty.
     * <p>
     * This method ensures that names used for countries and capital cities contain at least one character.
     * If the name is null or an empty string, an {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param name the name string to validate.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    private static void validateNames(final String name)
    {
        final boolean nameIsNull;
        final boolean nameIsEmpty;

        nameIsNull = name == null;
        nameIsEmpty = name != null && name.isEmpty();


        if(nameIsNull || nameIsEmpty)
        {
            throw new IllegalArgumentException("Empty or null name cannot be used.");
        }
    }

    /*
     * Validates that the provided facts array is neither null nor empty.
     * <p>
     * This method ensures that the country facts array is provided and contains at least one fact.
     * If the array is {@code null} or has a length of zero, an {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param facts the array of facts to validate.
     * @throws IllegalArgumentException if the facts array is null or empty.
     */
    private static void validateFacts(final String[] facts)
    {
        final boolean factsIsNull;
        final boolean factsIsEmpty;

        factsIsNull = facts == null;
        factsIsEmpty = facts != null && facts.length == ZERO_VALUE;

        if(factsIsNull || factsIsEmpty)
        {
            throw new IllegalArgumentException("Empty or null fact array cannot be used.");
        }
    }
}
