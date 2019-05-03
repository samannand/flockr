import superagent from "superagent";
import {endpoint} from "../../utils/endpoint";
import moment from "moment";

/**
 * Gets all of a users trips
 * @param {number} userId
 * @return {Object} The users trips
 */
export async function getTrips(userId) {
    const authToken = localStorage.getItem("authToken");

    const res = await superagent.get(endpoint(`/travellers/${userId}/trips`))
        .set("Authorization", authToken);
    return res.body;
}

/**
 * Finds the start date of the trip
 * @param {Object[]} tripDestinations
 */
export function findStart(tripDestinations) {
    for (const tripDestination of tripDestinations) {
        if (tripDestination.arrivalDate) {
            if (tripDestination.arrivalTime !== -1) {
                return moment(tripDestinations.arrivalDate).add(moment.duration(tripDestination.arrivalTime, "minutes"));
            } else {
                return moment(tripDestination.arrivalDate);
            }
        }

        if (tripDestination.departureDate) {
            if (tripDestination.departureTime !== -1) {
                return moment(tripDestinations.departureDate).add(moment.duration(tripDestination.departureTime, "minutes"));
            } else {
                return moment(tripDestination.departureDate);
            }
        }
    }
    return false;
}

/**
 * Finds the end date of the trip
 * @param {Object[]} tripDestinations
 */
export function findEnd(tripDestinations) {
    for (let i = tripDestinations.length - 1; i > -1; i--) {
        if (tripDestinations[i].departureDate) {
            if (tripDestinations[i].departureTime !== -1) {
                return moment(tripDestinations[i].departureDate).add(moment.duration(tripDestinations[i].departureTime, "minutes"));
            } else {
                return moment(tripDestinations[i].departureDate);
            }
        }

        if (tripDestinations[i].arrivalDate) {
            if (tripDestinations[i].arrivalTime !== -1) {
                return moment(tripDestinations[i].arrivalDate).add(moment.duration(tripDestinations[i].arrivalTime, "minutes"));
            } else {
                return moment(tripDestinations[i].arrivalDate);
            }
        }
    }
    return false;
}

/**
 * Transforms the trip to the right structure
 * @param {Object[]} trips
 */
export function transformTrips(trips) {

    return trips.map(trip => {
        const tripStart = findStart(trip.tripDestinations);
        const tripEnd = findEnd(trip.tripDestinations);
        const currentTime = moment();

        let tripStatus;

        if (!tripStart || !tripEnd) {
            tripStatus = "Unknown";
        } else if (currentTime.isAfter(tripStart) && currentTime.isBefore(tripEnd)) {
            tripStatus = "Ongoing";
        } else if (currentTime.isBefore(tripStart)) {
            tripStatus = "Upcoming";
        } else if (currentTime.isAfter(tripEnd)) {
            tripStatus = "Passed";
        } else {
            tripStatus = "Unknown";
        }

        return {
            tripId: trip.tripId,
            tripName: trip.tripName,
            status: tripStatus
        };
    });
}

/**
 * Sorts the trips by the trip start date. If the trip doesn't have a start date, the trip
 * gets put at the top
 * @param {Object[]} trips
 */
export function sortTrips(trips) {
    return trips.sort((tripA, tripB) => {
        const startA = findStart(tripA.tripDestinations);
        const startB = findStart(tripB.tripDestinations);
        if (!startA) return -1;
        if (!startB) return 1;
        return startB.diff(startA);
    });
}



