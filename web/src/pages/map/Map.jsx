import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import MapView from '../../components/map/mapView/MapView.jsx';
import styles from './Map.module.css';
import {findNearbyReports, getMyReports} from "../../api/reportApi.js";

const Map = ({ currentMode, setCurrentMode }) => {
    const [userLocation, setUserLocation] = useState({ lat: 0, lng: 0 });
    const [selectedCoords, setSelectedCoords] = useState(null);
    const [hazards, setHazards] = useState([]);
    const [activeAlerts] = useState([]);
    const [routeStart, setRouteStart] = useState(null);
    const [routeEnd, setRouteEnd] = useState(null);
    const [routeCoords, setRouteCoords] = useState([]);
    const [geoLoading, setGeoLoading] = useState(true);
    const [apiLoading, setApiLoading] = useState(false);

    const defaultCenter = [41.7151, 44.8271];

    const [searchParams] = useSearchParams();
    const viewMode = searchParams.get('view');

    // 2. Fetch reported hazards from your Spring Boot Backend API
    const loadNearbyHazards = useCallback(async () => {
        setApiLoading(true);
        try {
            let data;

            if (viewMode === 'mine') {
                data = await getMyReports();
            } else {
                // Otherwise, load geographic radius boundary hazards normally
                const savedRadius = parseFloat(localStorage.getItem('proximityRadius') || '5');
                const lat = userLocation.lat !== 0 ? userLocation.lat : defaultCenter[0];
                const lng = userLocation.lng !== 0 ? userLocation.lng : defaultCenter[1];
                data = await findNearbyReports(lat, lng, savedRadius);
            }

            setHazards(data);
        } catch (error) {
            console.error("Failed to fetch hazard points:", error);
        } finally {
            setApiLoading(false);
        }
    }, [userLocation, viewMode]);

    // 3. Track Browser Geolocation Coordinates
    useEffect(() => {
        if (!navigator.geolocation) {
            console.error("Geolocation is not supported by your browser");
            setGeoLoading(false);
            return;
        }

        const success = (position) => {
            setUserLocation({
                lat: position.coords.latitude,
                lng: position.coords.longitude
            });
            setGeoLoading(false);
        };

        const error = (err) => {
            console.warn(`Geolocation error (${err.code}): ${err.message}`);
            setGeoLoading(false);
        };

        navigator.geolocation.getCurrentPosition(success, error, {
            enableHighAccuracy: true,
            timeout: 1000000,
            maximumAge: 0
        });
    }, []);

    useEffect(() => {
        loadNearbyHazards().catch(err => console.error("Initial load failed:", err));
    }, [loadNearbyHazards]);

    // 4. Handle clicks on the map grid canvas context
    const handleMapClick = (e) => {
        if (currentMode === 'report') {
            setSelectedCoords({ lat: e.lat, lng: e.lng });
        } else if (currentMode === 'route') {
            if (!routeStart || (routeStart && routeEnd)) {
                setRouteStart({ lat: e.lat, lng: e.lng });
                setRouteEnd(null);
                setRouteCoords([])
            } else {
                setRouteEnd({ lat: e.lat, lng: e.lng });
            }
        }
    };

    const handleCancelClick = () => {
        setSelectedCoords(null);
    };

    return (
        <div className={styles.mapPageContainer}>
            <MapView
                currentMode={currentMode}
                setCurrentMode={setCurrentMode}
                userLocation={userLocation}
                defaultCenter={defaultCenter}
                hazards={hazards}
                selectedCoords={selectedCoords}
                setSelectedCoords={setSelectedCoords}
                activeAlerts={activeAlerts}
                geoLoading={geoLoading}
                apiLoading={apiLoading}
                handleMapClick={handleMapClick}
                handleCancel={handleCancelClick}
                loadNearbyHazards={loadNearbyHazards}
                routeStart={routeStart}
                routeEnd={routeEnd}
                routeCoords={routeCoords}
                setRouteStart={setRouteStart}
                setRouteEnd={setRouteEnd}
                setRouteCoords={setRouteCoords}

            />
        </div>
    );
};

export default Map;