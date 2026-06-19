import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import styles from './MapView.module.css';
import {MapClickHandler} from "../MapClickHandler.jsx";
import ReportForm from '../reportForm/ReportForm';
import RecenterMap from '../RecenterMap';

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
                     routeCoords,
                     setRouteCoords
                 }) => {
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
            </div>

            {/* Leaflet Dynamic Interactive Map Layer */}
            <MapContainer
                center={userLocation && userLocation.lat !== 0 ? [userLocation.lat, userLocation.lng] : defaultCenter}
                zoom={14}
                className={styles.leafletContainer}
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                <MapClickHandler
                    mode={currentMode}
                    onMapClicked={handleMapClick}
                />

                {currentMode === 'route' && routeCoords && (
                    <Marker position={[routeCoords.lat, routeCoords.lng]}>
                        <Popup>
                            <div style={{ padding: '4px', textAlign: 'center' }}>
                                <strong>Route Destination</strong>
                                <br />
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setRouteCoords(null);
                                    }}
                                    style={{ marginTop: '5px', padding: '2px 6px', fontSize: '11px', cursor: 'pointer' }}
                                >
                                    Clear Pin
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                )}

                {userLocation && userLocation.lat !== 0 && <RecenterMap location={userLocation} />}

                {userLocation && userLocation.lat !== 0 && (
                    <Marker position={[userLocation.lat, userLocation.lng]}>
                        <Popup>
                            <div style={{ textAlign: 'center', fontWeight: 'bold', color: '#1e293b' }}>
                                Your Position
                            </div>
                        </Popup>
                    </Marker>
                )}

                {/* Map Pins from Backend Database array */}
                {hazards.map((hazard) => (
                    <Marker key={hazard.id || `${hazard.lat}-${hazard.lng}`} position={[hazard.lat, hazard.lng]}>
                        <Popup>
                            <div style={{ padding: '2px', minWidth: '120px' }}>
                                <h3 style={{ margin: '0 0 4px 0', borderBottom: '1px solid #e2e8f0', paddingBottom: '4px', fontWeight: 'bold' }}>
                                    {hazard.type || 'Reported Incident'}
                                </h3>
                                <p style={{ fontSize: '12px', margin: '4px 0 0 0' }}>
                                    {hazard.description || 'No context supplied.'}
                                </p>
                                {hazard.distanceFromUser && (
                                    <p style={{ fontSize: '10px', color: '#94a3b8', margin: '4px 0 0 0' }}>
                                        Distance: {Math.round(hazard.distanceFromUser)}m away
                                    </p>
                                )}
                            </div>
                        </Popup>
                    </Marker>
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