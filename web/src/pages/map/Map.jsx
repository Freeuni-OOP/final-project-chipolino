import React, { useState, useEffect, useCallback } from 'react';
import MapView from '../../components/map/mapView/MapView.jsx';
import styles from './Map.module.css';

const Map = ({ currentMode, setCurrentMode }) => {
    const [userLocation, setUserLocation] = useState({ lat: 0, lng: 0 });
    const [selectedCoords, setSelectedCoords] = useState(null);
    const [hazards, setHazards] = useState([]);
    const [activeAlerts] = useState([]);
    const [routeCoords, setRouteCoords] = useState(null);
    const [geoLoading, setGeoLoading] = useState(true);
    const [apiLoading, setApiLoading] = useState(false);

    const defaultCenter = [41.7151, 44.8271];

    // 2. Fetch reported hazards from your Spring Boot Backend API
    const loadNearbyHazards = useCallback(async () => {
        setApiLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/reports');
            if (response.ok) {
                const data = await response.json();
                setHazards(data);
            }
        } catch (error) {
            console.error("Failed to fetch hazard points:", error);
        } finally {
            setApiLoading(false);
        }
    }, []);

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
            timeout: 5000,
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
            setRouteCoords({ lat: e.lat, lng: e.lng });
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
                routeCoords={routeCoords}
                setRouteCoords={setRouteCoords}
            />
        </div>
    );
};

export default Map;