import { useEffect } from 'react';
import { useMap } from 'react-leaflet';

const RecenterMap = ({ location }) => {
    /** @type {any} */
    const map = useMap();

    useEffect(() => {
        if (location && location.lat != null && location.lng != null) {
            map.setView([location.lat, location.lng], 14);
        }
    }, [location, map]);

    return null;
};

export default RecenterMap;