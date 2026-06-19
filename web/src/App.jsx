import React, { useState } from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes/AppRoutes';
import Navbar from "./components/layout/Navbar.jsx";

function App() {
    const [currentMode, setCurrentMode] = useState('route');

    return (
        <Router>
            <AuthProvider>
                <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100%' }}>

                    <Navbar />

                    <main style={{ flex: 1, width: '100%', position: 'relative' }}>
                        <AppRoutes currentMode={currentMode} setCurrentMode={setCurrentMode} />
                    </main>

                </div>
            </AuthProvider>
        </Router>
    );
}

export default App;