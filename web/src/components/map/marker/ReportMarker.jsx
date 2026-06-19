import styles from './ReportMarker.module.css';
import {Marker} from "react-leaflet";
import L from 'leaflet';
import {ReportPopup} from '../popup/ReportPopUp.jsx';

const getEmojiByType = (type) => {
    switch (type) {
        case 'POTHOLE':  return '🕳️'
        case 'ACCIDENT': return '💥'
        case 'HEAVY_TRAFFIC': return '🚗'
        case 'ROAD_CLOSURE': return '🚧'
        case 'SPEED_CAMERA': return '📸'
        case 'POLICE': return '🚓'
        case 'CUSTOM': return '⚠️'
        default: return '📍'
    }
}

const getBrightnessByStatus = (status) => {
    switch (status) {
        case 'NOT_VERIFIED_PERMANENT': return 0.6
        case 'TEMPORARY':             return 0.85
        case 'PERMANENT':             return 1.0
        default:                      return 1.0
    }
}


/**
 * A marker component for the map that visually represents a specific report.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Filters out reports marked as 'REMOVED' so they are not rendered on the map.</li>
 * <li>Determines the appropriate visual emoji based on the report type.</li>
 * <li>Calculates visual brightness based on the report status.</li>
 * <li>Creates a custom Leaflet {@link L.divIcon} using the computed emoji and styles.</li>
 * <li>Renders a {@link Marker} at the report's coordinates, containing a {@link ReportPopup}
 * for interactive details.</li>
 * </ul>
 * </p>
 * @param report The report object containing data.
 * @returns {@link Marker} component if the report is active, or {@code null} if the report is removed.
 */
export const ReportMarker = ({report}) => {
    const { latitude, longitude, type, status } = report
    if (status?.toUpperCase() === 'REMOVED'){
        return null
    }

    const emoji = getEmojiByType(type)
    const brightness = getBrightnessByStatus(status)
    const roadIcon = L.divIcon({
        html: `<div class='${styles.hazardEmojiMarker}'
                      style='--marker-brightness: ${brightness}'>
                    ${emoji}
                </div>`,
        className: styles.customHazardContainer,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -15]
    })

    return (
        <Marker position={[latitude, longitude]}
                icon={roadIcon}>
            <ReportPopup report={report} />
        </Marker>
    )
}