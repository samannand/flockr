package models;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
public class DestinationTest {

    DestinationType destinationType;
    District destinationDistrict;
    Double destinationLat;
    Double destinationLon;
    Country destinationCountry;
    Integer destinationOwner;
    boolean isPublic;

    @Before
    public void setUp() throws Exception {
        destinationCountry = new Country("New Zealand");
        destinationType = new DestinationType("Backpacker");
        destinationDistrict = new District("Ilam", destinationCountry);
        destinationLat = 3.0;
        destinationLon = 45.0;
    }

    @Test
    public void twoDestinationsArePerfectlyEqual() {
        Destination destination1 = new Destination("England", destinationType,
                destinationDistrict, destinationLat, destinationLat, destinationCountry, destinationOwner, isPublic);
        Destination destination2 = new Destination("England", destinationType,
                destinationDistrict, destinationLat, destinationLat, destinationCountry, destinationOwner, isPublic);

        Assert.assertEquals(destination1, destination2);
    }

    /**
     * Check the equality fails when the destinations being compared have different types.
     */
    @Test
    public void twoDestinationsAreUnequal() {
        DestinationType type = new DestinationType("Attraction");
        Destination destination1 = new Destination("Colosseum", destinationType,
                destinationDistrict, destinationLat, destinationLon, destinationCountry, destinationOwner, isPublic);
        Destination destination2 = new Destination("Colosseum", type,
                destinationDistrict, destinationLat, destinationLon, destinationCountry, destinationOwner, isPublic);

        Assert.assertNotEquals(destination1, destination2);
    }

    /**
     * Destinations are considered "equal" if they have the same name, type and district.
     * This test ensures they can have differences in other properties (lat, lon).
     */
    @Test
    public void twoDestinationsMeetEquality() {
        Double lat1 = 10.0;
        Double lon1 = 10.0;
        Double lat2 = 20.0;
        Double lon2 = 20.0;
        Destination destination1 = new Destination("Big Ben", destinationType, destinationDistrict,
                lat1, lon1, destinationCountry, destinationOwner, isPublic);
        Destination destination2 = new Destination("Big Ben", destinationType, destinationDistrict,
                lat2, lon2, destinationCountry, destinationOwner, isPublic);

        Assert.assertEquals(destination1, destination2);
    }

    /**
     * There is the possibility of destinations being created with a country
     * different to the destination's district country.
     * This test ensures that the equality is false in that scenario.
     */
    @Test
    public void twoDestinationsHaveDifferentCountries() {
        Country country1 = new Country("India");
        Country country2 = new Country("China");
        Destination destination1 = new Destination("Atlantis", destinationType, destinationDistrict,
                destinationLat, destinationLon, country1, destinationOwner, isPublic);
        Destination destination2 = new Destination("Atlantis", destinationType, destinationDistrict,
                destinationLat, destinationLon, country2, destinationOwner, isPublic);

        Assert.assertNotEquals(destination1, destination2);
    }
}