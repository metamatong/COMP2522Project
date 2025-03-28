package ca.bcit.comp2522.project;

/**
 * A class that represents country information used in the game.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
class Country
{
    private final String name;
    private final String capitalCityName;
    private final String[] facts;

    /*
     * A constructor that instantiates Country object.
     *
     * @param name for name of the country
     * @param capitalCityName for name of the capital city of the given country
     * @param facts for simple facts about the given country
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
     * validates String names used in Country class in the constructor.
     * @param name is the String name to be validated.
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
     * validates String array facts used in Country class in the constructor.
     * @param facts is the String array of facts to be validated.
     */
    private static void validateFacts(final String[] facts)
    {
        final boolean factsIsNull;
        final boolean factsIsEmpty;

        factsIsNull = facts == null;
        factsIsEmpty = facts != null && facts.length == 0;

        if(factsIsNull || factsIsEmpty)
        {
            throw new IllegalArgumentException("Empty or null fact array cannot be used.");
        }
    }
}
