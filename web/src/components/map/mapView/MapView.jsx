import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import styles from './MapView.module.css';
import routePopupStyles from './RoutePopup.module.css';
import {MapClickHandler} from "../MapClickHandler.jsx";
import ReportForm from '../reportForm/ReportForm';
import RecenterMap from '../RecenterMap';
import RoutePanel from '../routePanel/RoutePanel';
import { ReportMarker } from '../marker/ReportMarker.jsx';
import {useAuth} from "../../../hooks/useAuth.js";

const worldBounds = [
    [-90, -180],
    [90, 180]
];

const MapView = ({
                     currentMode,
                     setCurrentMode,
                     userLocation,
                     defaultCenter,
                     hazards = [],
                     selectedCoords,
                     setSelectedCoords,
                     activeAlerts = [],
                     handleMapClick,
                     handleCancel,
                     loadNearbyHazards,
                     routeStart,
                     routeEnd,
                     routeCoords,
                     setRouteStart,
                     setRouteEnd,
                     setRouteCoords
                 }) => {


    const { user } = useAuth();
    const markerPosition = selectedCoords ? [selectedCoords.lat, selectedCoords.lng] : null;
    return (
        <div className={styles.mapWrapper}>

            {/* Proximity Alerts Layer */}
            {activeAlerts.length > 0 && (
                <div className={styles.proximityAlert}>
                    <p style={{ margin: 0, fontWeight: 'bold' }}>
                        ⚠️ PROXIMITY ALERT: {activeAlerts.length} Danger(s) Imminent!
                    </p>
                    <ul style={{ margin: '4px 0 0', paddingLeft: '16px', fontSize: '12px' }}>
                        {activeAlerts.map(alert => (
                            <li key={alert.id || alert.lat}>
                                {alert.type || 'Hazard'} (~{Math.round(alert.distanceFromUser)}m away)
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            <div className={styles.dashboardPanel}>
                <h3>Mode: {currentMode === 'route' ? 'Route Navigation' : 'Report Mode (Click Map)'}</h3>

                <button
                    className={`${styles.modeButton} ${currentMode === 'route' ? styles.active : ''}`}
                    onClick={() => setCurrentMode('route')}
                >
                    Route Mode
                </button>

                <button
                    className={`${styles.modeButton} ${currentMode === 'report' ? styles.active : ''}`}
                    onClick={() => setCurrentMode('report')}
                >
                    Report Mode
                </button>

                {currentMode === 'route' && (
                    <RoutePanel
                        startPoint={routeStart}
                        endPoint={routeEnd}
                        onRouteCalculated={(points) => {
                            setRouteCoords(points);
                        }}
                        onClearRoute={() => {
                            setRouteStart(null);
                            setRouteEnd(null);
                            setRouteCoords([]);
                        }}
                    />
                )}
            </div>

            {/* Leaflet Dynamic Interactive Map Layer */}
            <MapContainer
                center={userLocation && userLocation.lat !== 0 ? [userLocation.lat, userLocation.lng] : defaultCenter}
                zoom={14}
                minZoom={2.5}
                maxBounds={worldBounds}
                maxBoundsViscosity={1.0}
                className={styles.leafletContainer}
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    noWrap={true}
                />

                <MapClickHandler
                    mode={currentMode}
                    onMapClicked={handleMapClick}
                />

                {routeCoords && routeCoords.length > 0 && (
                    <Polyline positions={routeCoords} pathOptions={{ color: '#38bdf8', weight: 5, opacity: 0.85, lineJoin: 'round' }} />
                )}

                {currentMode === 'route' && routeStart && (
                    <Marker position={[routeStart.lat, routeStart.lng]}>
                        <Popup minWidth={160}>
                            <div className={routePopupStyles.popupContent}>
                                <strong className={routePopupStyles.popupTitle}>📍 Origin (Start)</strong>
                                <button
                                    className={routePopupStyles.popupButton}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setRouteStart(null);
                                    }}
                                >
                                    Clear Start
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                )}
                {currentMode === 'route' && routeEnd && (
                    <Marker position={[routeEnd.lat, routeEnd.lng]}>
                        <Popup minWidth={160}>
                            <div className={routePopupStyles.popupContent}>
                                <strong className={routePopupStyles.popupTitle}>🏁 Destination (End)</strong>
                                <button
                                    className={routePopupStyles.popupButton}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setRouteEnd(null);
                                    }}
                                >
                                    Clear Destination
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                )}

                {userLocation && userLocation.lat !== 0 && <RecenterMap location={userLocation} />}

                {userLocation && userLocation.lat !== 0 && (
                    <Marker position={[userLocation.lat, userLocation.lng]}>
                        <Popup>
                            <div style={{ textAlign: 'center', fontWeight: 'bold', color: '#848485' }}>
                                Your Position
                            </div>
                        </Popup>
                    </Marker>
                )}

                {/* Map Pins from Backend Database array */}
                {hazards.map((hazard) => (
                    <ReportMarker
                        key={hazard.id || `${hazard.latitude}-${hazard.longitude}`}
                        report={hazard}
                        currentUser={user}
                    />
                ))}

                {selectedCoords !== null && markerPosition && (
                    <Marker position={markerPosition}>
                        <Popup minWidth={240}>
                            <div onClick={(e) => e.stopPropagation()} onMouseDown={(e) => e.stopPropagation()}>
                                <ReportForm
                                    location={selectedCoords}
                                    onClose={() => {
                                        setSelectedCoords(null);
                                        if (handleCancel) handleCancel();
                                    }}
                                    onSuccess={() => {
                                        setSelectedCoords(null);
                                        loadNearbyHazards().catch(err => console.error(err));
                                    }}
                                />
                            </div>
                        </Popup>
                    </Marker>
                )}
            </MapContainer>
        </div>
    );
};

export default MapView;