import {useCallback, useEffect, useState} from "react";

/**
 * Custom hook to get and manage the user's current location
 *  @returns {{
 * location: {lat: number, lng: number},
 * error: string|null,
 * loading: boolean,
 * refreshLocation: function
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

    const refreshLocation = useCallback(() => {
        if(!navigator.geolocation){
            setError('Geolocation does not work in this browser')
            setLoading(false)
            return;
        }

        setLoading(true)
        navigator.geolocation.getCurrentPosition(handleSuccess, handleError)

    }, [handleSuccess, handleError])

    useEffect(() => {
        refreshLocation()
    }, [refreshLocation])

    return {location, error, loading, refreshLocation}
}