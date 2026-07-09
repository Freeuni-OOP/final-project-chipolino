import { useEffect, useState } from "react";

/**
 * Calculates the distance between two points on the Earth's surface
 * using the Haversine formula.
 * @param {number} lat1 - Latitude of the first point in decimal degrees.
 * @param {number} lon1 - Longitude of the first point in decimal degrees.
 * @param {number} lat2 - Latitude of the second point in decimal degrees.
 * @param {number} lon2 - Longitude of the second point in decimal degrees.
 * @returns {number} The straight-line distance between the two points in meters.
 */
const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371000; //Earth's radius in meters
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;

    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
};

/**
 * Hook to track user proximity to the road hazards.
 * Uses the Haversine formula to calculate spherical distance.
 * @param {Object} userLocation - The current GPS coordinates of the user.
 * @param {number} userLocation.lat - User's current latitude.
 * @param {number} userLocation.lng - User's current longitude.
 * @param {Array<Object>} hazards - Array of reported road hazards from the database.
 * @param {number} radiusInMeters - The boundary radius to trigger an alert.
 * @returns {Array<Object>} Filtered list of hazards within the radius.
 */
export const useProximityAlerts = (userLocation, hazards, radiusInMeters = 50) => {
    const [activeAlerts, setActiveAlerts] = useState([]);

    useEffect(() => {
        if (!userLocation || !hazards || hazards.length === 0) {
            setActiveAlerts([]);
            return;
        }

        const nearbyHazards = hazards.map((hazard) => {
            const distance = calculateDistance(
                userLocation.lat,
                userLocation.lng,
                hazard.latitude,
                hazard.longitude
            );

            return { ...hazard, distanceFromUser: distance };
        }).filter((hazardWithDistance) => hazardWithDistance.distanceFromUser <= radiusInMeters);

        setActiveAlerts(nearbyHazards);
    }, [userLocation, hazards, radiusInMeters]);

    return activeAlerts;
}