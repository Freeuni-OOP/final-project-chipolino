import React, { useState } from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes/AppRoutes';
import Navbar from "./components/layout/Navbar.jsx";
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: markerIcon2x,
    iconUrl: markerIcon,
    shadowUrl: markerShadow
});

function App() {
    const [currentMode, setCurrentMode] = useState('route');
    const [followUser, setFollowUser] = useState(true);

    return (
        <Router>
            <AuthProvider>
                <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100%' }}>

                    <Navbar followUser={followUser}
                            setFollowUser={setFollowUser}
                    />

                    <main style={{ flex: 1, width: '100%', position: 'relative' }}>
                        <AppRoutes currentMode={currentMode}
                                   setCurrentMode={setCurrentMode}
                                   followUser={followUser}
                                   setFollowUser={setFollowUser}
                        />
                    </main>

                </div>
            </AuthProvider>
        </Router>
    );
}

export default App;