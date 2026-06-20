import { useState } from 'react';
import { Button } from '../../common/button/Button';
import { Spinner } from '../../common/spinner/Spinner';
import { calculateOptimalRoute } from '../../../api/routeApi.js';

import styles from './RoutePanel.module.css';

const RoutePanel = ({ startPoint, endPoint, onRouteCalculated, onClearRoute }) => {
    const [distance, setDistance] = useState(null);
    const [duration, setDuration] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');


    const handleClear = () => {
        setDistance(null);
        setDuration(null);
        setError('');

        if (onClearRoute) {
            onClearRoute();
        }
    };

    const handleCalculate = async () => {
        if (!startPoint || !endPoint) {
            setError('Please select both Origin and Destination on the map.');
            return;
        }

        setIsLoading(true);
        setError('');
        setDistance(null);
        setDuration(null);

        try {
            const routeData = {
                waypoints: [
                    [startPoint.lat, startPoint.lng],
                    [endPoint.lat, endPoint.lng]
                ]
            };

            console.log('Sending route request:', routeData);

            const response = await calculateOptimalRoute(routeData);

            const formattedDistance = (response.distanceMeters / 1000).toFixed(1) + ' km';
            const formattedDuration = Math.round(response.timeMillis / 60000) + ' mins';

            setDistance(formattedDistance);
            setDuration(formattedDuration);

            if (onRouteCalculated && response.points) {
                onRouteCalculated(response.points);
            }
        } catch (err) {
            console.error('Routing error:', err);
            setError('Failed to calculate route. Please try again later.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.panelContainer}>
            <h3 className={styles.title}>Route Planner</h3>

            <div className={styles.pointsDisplay}>
                <div className={`${styles.pointItem} ${startPoint ? styles.active : ''}`}>
                    <strong>📍 Origin:</strong> {startPoint ? 'Selected' : 'Click on map'}
                </div>
                <div className={`${styles.pointItem} ${endPoint ? styles.active : ''}`}>
                    <strong>🏁 Destination:</strong> {endPoint ? 'Selected' : 'Click on map'}
                </div>
            </div>

            {error && <div className={styles.errorBox}>{error}</div>}

            {distance && duration && (
                <div className={styles.resultBox}>
                    <div className={styles.resultItem}>
                        <span className={styles.resultLabel}>Distance:</span>
                        <span className={styles.resultValue}>{distance}</span>
                    </div>
                    <div className={styles.resultItem}>
                        <span className={styles.resultLabel}>Est. Time:</span>
                        <span className={styles.resultValue}>{duration}</span>
                    </div>
                </div>
            )}

            <div className={styles.buttonGroup}>
                <button
                    type="button"
                    className={styles.clearBtn}
                    onClick={handleClear}
                    disabled={isLoading || (!startPoint && !endPoint && !distance)}
                >
                    Clear
                </button>

                <Button
                    onClick={handleCalculate}
                    disabled={isLoading || !startPoint || !endPoint}
                    className={styles.calcBtn}
                >
                    {isLoading ? <Spinner /> : 'Find Route'}
                </Button>
            </div>
        </div>
    );

};


export default RoutePanel;