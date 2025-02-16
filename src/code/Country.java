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

    /*
     * validates String names used in Country class in the constructor.
     * @param name is the String name to be validated.
     */
    private static void validateNames(final String name)
    {
        final boolean nameIsNull;
        nameIsNull = name == null;

        final boolean nameIsEmpty;
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
        factsIsNull = facts == null;

        final boolean factsIsEmpty;
        factsIsEmpty = facts != null && facts.length == 0;

        if(factsIsNull || factsIsEmpty)
        {
            throw new IllegalArgumentException("Empty or null fact array cannot be used.");
        }
    }
}
