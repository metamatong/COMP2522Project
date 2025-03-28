package ca.bcit.comp2522.project;

import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * A class that represents the world with its consisting countries.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
class World
{
    private static final int FIRST_LINE_DATA_INDEX = 0;
    private static final int SECOND_LINE_DATA_INDEX = 1;
    private static final int ARRAY_SPLIT_LIMIT_FOR_COUNTRY_HEADER = 2;
    private static final int EMPTY_ARRAY_LENGTH = 0;

    private final Map<String, Country> countries;

    /*
     * Constructs a World with the given countries.
     *
     * @param countries the map of country names to Country objects
     * @throws IOException any error happened during reading a text file
     */
    World()
    {
        final HashMap<String, Country> countriesMap;
        countriesMap = new HashMap<>();

        for(char letter = 'a'; letter <= 'z'; letter++)
        {
            final String fileName;
            fileName = "src/res/data/" + letter + ".txt";

            final Path filePath;
            filePath = Paths.get(fileName);

            // Check if the file exists; if not, skip to the next iteration.
            if(!Files.exists(filePath))
            {
                System.out.println("File " + fileName + " not found. Skipping.");
                continue;
            }

            try
            {
                final List<String> lines;
                lines = Files.readAllLines(Paths.get(fileName));

                // A block represents one country's data.
                List<String> block;
                block = new ArrayList<>();

                for(final String line : lines)
                {
                    if(line.trim().isEmpty())
                    {
                        // Blank line indicates end of a country block.
                        if(!block.isEmpty())
                        {
                            addCountryFromBlock(block, countriesMap);
                            block.clear();
                        }
                    }
                    else
                    {
                        block.add(line);
                    }
                }
                // Process any remaining block in case the file does not end with a blank line.
                if(!block.isEmpty())
                {
                    addCountryFromBlock(block, countriesMap);
                }
            }
            catch(final IOException e)
            {
                System.err.println("Error reading file " + fileName + ": " + e.getMessage());
            }
        }

        // Validate the map and assign it to the instance variable.
        validateCountries(countriesMap);
        this.countries = countriesMap;
    }

    /**
     * A getter for countries stored in World object.
     * @return a map of countries with String keys and Country object values
     */
    public Map<String, Country> getCountries()
    {
        return this.countries;
    }

    /*
     * Validates the countries map used in the World constructor.
     *
     * @param countries is the Map of countries to be validated.
     */
    private static void validateCountries(final HashMap<String, Country> countries)
    {
        final boolean countriesIsNull;
        final boolean countriesIsEmpty;

        countriesIsNull = countries == null;
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
            final boolean nameIsEmpty;

            nameIsNull = countryName == null;
            nameIsEmpty = countryName != null && countryName.isEmpty();

            if(nameIsNull || nameIsEmpty)
            {
                throw new IllegalArgumentException("Empty or null country name cannot be used in the map.");
            }

            // Validate that the Country object itself is not null
            if(country == null)
            {
                throw new IllegalArgumentException("Null Country cannot be used in the countries map.");
            }
        }
    }

    /*
     * A helper method to parse a block of lines representing a single country and add it to the map.
     *
     * Expected block format:
     *   CountryName:CapitalCityName
     *   Fact 1
     *   Fact 2
     *   ...
     *
     * @param block        the list of lines for one country
     * @param countriesMap the map to add the Country object to
     */
    private static void addCountryFromBlock(final List<String> block,
                                            final Map<String, Country> countriesMap)
    {
        // The first line should be in the format "CountryName:CapitalCityName"
        final String header;
        final String[] headerParts;

        header = block.get(FIRST_LINE_DATA_INDEX);
        headerParts = header.split(":", ARRAY_SPLIT_LIMIT_FOR_COUNTRY_HEADER);

        if(headerParts.length < ARRAY_SPLIT_LIMIT_FOR_COUNTRY_HEADER)
        {
            System.err.println("Invalid header format: " + header);
            return;
        }

        final String countryName;
        final String capitalCityName;

        countryName = headerParts[FIRST_LINE_DATA_INDEX].trim();
        capitalCityName = headerParts[SECOND_LINE_DATA_INDEX].trim();

        // The remaining lines are facts.
        final List<String> factsList;
        factsList = new ArrayList<>();

        for(int i = SECOND_LINE_DATA_INDEX; i < block.size(); i++)
        {
            final String fact;
            fact = block.get(i).trim();

            if(!fact.isEmpty())
            {
                factsList.add(fact);
            }
        }

        final String[] facts;
        facts = factsList.toArray(new String[EMPTY_ARRAY_LENGTH]);

        try
        {
            final Country country;
            country = new Country(countryName, capitalCityName, facts);
            countriesMap.put(countryName, country);
        }
        catch(final IllegalArgumentException e)
        {
            System.err.println("Failed to add country '" + countryName + "': " + e.getMessage());
        }
    }
}
