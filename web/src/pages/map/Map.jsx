import React, { useState, useEffect, useCallback } from 'react';
import MapView from '../../components/map/mapView/MapView.jsx';
import styles from './Map.module.css';
import {findNearbyReports} from "../../api/reportApi.js";

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

    // 2. Fetch reported hazards from your Spring Boot Backend API
    const loadNearbyHazards = useCallback(async () => {
        setApiLoading(true);
        try {
            const savedRadius = parseFloat(localStorage.getItem('proximityRadius') || '5');
            const lat = userLocation.lat !== 0 ? userLocation.lat : defaultCenter[0];
            const lng = userLocation.lng !== 0 ? userLocation.lng : defaultCenter[1];
            const data = await findNearbyReports(lat, lng, savedRadius);

            setHazards(data);
        } catch (error) {
            console.error("Failed to fetch hazard points:", error);
        } finally {
            setApiLoading(false);
        }
    }, [userLocation]);

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
                routePoints={routeCoords}
                setRouteStart={setRouteStart}
                setRouteEnd={setRouteEnd}
                setRoutePoints={setRouteCoords}

            />
        </div>
    );
};

export default Map;