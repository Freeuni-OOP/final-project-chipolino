import {useCallback, useEffect, useState} from "react";

/**
 * Custom hook to get and manage the user's current location
 *  @returns {{
 * location: {lat: number, lng: number},
 * error: string|null,
 * loading: boolean,
 * }}
 * An object containing the coordinates, error messages,
 * loading state, and a function for manual refresh.
 */
export const useGeolocation = () => {
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [location, setLocation] =
        useState({lat : 0, lng : 0})

    const handleSuccess = useCallback((position) => {
        setLocation({lat : position.coords.latitude,
            lng : position.coords.longitude})

        setError(null)
        setLoading(false)
    }, [])

    const handleError = useCallback(() => {
        setError("Error when getting location")
        setLoading(false)
    }, [])

    useEffect(() => {
        if(!navigator.geolocation){
            setError('Geolocation does not work in this browser')
            setLoading(false)
            return;
        }

        const geoOptions = {
            enableHighAccuracy: true,
            timeout: 100000,
            maximumAge: 0
        };

        setLoading(true)
        const watchId =
            navigator.geolocation.watchPosition(handleSuccess, handleError, geoOptions)

        return () => navigator.geolocation.clearWatch(watchId)
    }, [handleSuccess, handleError])

    return {location, error, loading}
}