package ca.bcit.comp2522.project;

import java.util.Map;
import java.util.HashMap;

/**
 * A class that represents the world with its consisting countries.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
class World
{
    private final Map<String, Country> countries;

    /*
     * Constructs a World with the given countries.
     *
     * @param countries the map of country names to Country objects
     */
    World(final HashMap<String, Country> countries)
    {
        validateCountries(countries);
        this.countries = countries;
    }

    /*
     * Validates the countries map used in the World constructor.
     *
     * @param countries is the Map of countries to be validated.
     */
    private static void validateCountries(final HashMap<String, Country> countries)
    {
        final boolean countriesIsNull;
        countriesIsNull = countries == null;

        final boolean countriesIsEmpty;
        countriesIsEmpty = countries != null && countries.isEmpty();

        if(countriesIsNull || countriesIsEmpty)
        {
            throw new IllegalArgumentException("Empty or null countries map cannot be used.");
        }

        // Validate each key and Country entry in the map.
        for (final Map.Entry<String, Country> entry : countries.entrySet())
        {
            final String countryName = entry.getKey();
            final Country country = entry.getValue();

            // Validate the country name
            final boolean nameIsNull;
            nameIsNull = countryName == null;

            final boolean nameIsEmpty;
            nameIsEmpty = countryName != null && countryName.isEmpty();

            if(nameIsNull || nameIsEmpty)
            {
                throw new IllegalArgumentException("Empty or null country name cannot be used in the map.");
            }

            // Validate that the Country object itself is not null
            if (country == null)
            {
                throw new IllegalArgumentException("Null Country cannot be used in the countries map.");
            }
        }
    }
}
