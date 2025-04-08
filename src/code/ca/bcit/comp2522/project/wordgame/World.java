package ca.bcit.comp2522.project.wordgame;

import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Represents a world containing a collection of countries used in the game.
 * <p>
 * The {@code World} class loads country data from text files located in the "src/res/data/" directory.
 * Each file is named with a lowercase letter (a–z) and may contain multiple country records. A country record
 * is defined as a block of text lines where:
 * <ul>
 *   <li>The first line is a header in the format "CountryName:CapitalCityName".</li>
 *   <li>The subsequent lines contain facts about the country.</li>
 *   <li>A blank line indicates the end of a country record.</li>
 * </ul>
 * The parsed countries are stored in a map with the country name as the key and a corresponding {@link Country}
 * object as the value.
 * </p>
 * <p>
 * If any file is missing or if a country record is malformed, the class logs the error and skips the problematic
 * record. After processing all files, the country map is validated to ensure it is not empty or null.
 * </p>
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

    /**
     * Constructs a World with the given countries.
     *
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
     * Returns the map of countries loaded into this world.
     * <p>
     * Each entry in the map uses the country name as the key and its corresponding {@link Country} object as the value.
     * </p>
     *
     * @return a map of country names to {@link Country} objects.
     */
    public Map<String, Country> getCountries()
    {
        return this.countries;
    }


    /*
     * Validates the provided countries map to ensure it is not null or empty and that its entries are valid.
     * <p>
     * This method checks if the map is {@code null} or empty, and then verifies that each key (country name)
     * is not null or empty, and that each value (the {@link Country} object) is not null.
     * </p>
     *
     * @param countries the map of countries to validate.
     * @throws IllegalArgumentException if the countries map is null, empty, or contains invalid entries.
     */
    private static void validateCountries(final HashMap<String, Country> countries)
    {
        final boolean countriesNull;
        final boolean countriesEmpty;

        countriesNull = countries == null;
        countriesEmpty = countries != null && countries.isEmpty();

        if(countriesNull || countriesEmpty)
        {
            throw new IllegalArgumentException("Empty or null countries map cannot be used.");
        }

        // Validate each key and Country entry in the map.
        for(final Map.Entry<String, Country> entry : countries.entrySet())
        {
            final String countryName = entry.getKey();
            final Country country = entry.getValue();

            // Validate the country name
            final boolean nameNull;
            final boolean nameEmpty;

            nameNull = countryName == null;
            nameEmpty = countryName != null && countryName.isEmpty();

            if(nameNull || nameEmpty)
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
     * Parses a block of text lines representing a single country's data and adds the resulting {@link Country} object
     * to the specified countries map.
     * <p>
     * The block is expected to be formatted as follows:
     * <ul>
     *   <li>The first line (header) must be in the format "CountryName:CapitalCityName".</li>
     *   <li>The subsequent lines contain facts about the country.</li>
     * </ul>
     * If the header line is malformed or any validation fails in the {@link Country} constructor,
     * an error message is logged and the block is skipped.
     * </p>
     *
     * @param block        the list of text lines representing one country's data.
     * @param countriesMap the map to which the parsed {@link Country} object should be added.
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
