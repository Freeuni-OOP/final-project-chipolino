import {useMapEvents} from "react-leaflet";

/**
 * Component that listens for click events on the Leaflet map instance.
 * Captures geographic coordinates and forwards them to the parent component.
 * @param {Object} props
 * @param {String} props.mode - The current operational mode of the application layout.
 * @param {Function} props.onMapClicked - Callback triggered when the map is clicked, receiving `{lat, lng}`.
 * @returns {null} Renders no structural DOM elements.
 */
export const MapClickHandler = ({mode, onMapClicked}) => {
    useMapEvents({
        click(e){
            const {lat, lng} = e.latlng;
            console.log(`Map clicked at lat: ${lat} and lng: ${lng}. In mode ${mode}`);

            onMapClicked({lat, lng});
        }
    })
    return null;
}