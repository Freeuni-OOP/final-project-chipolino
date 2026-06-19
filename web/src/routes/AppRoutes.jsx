import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import Map from '../pages/map/Map.jsx';
import Login from '../pages/login/Login.jsx';
import Register from '../pages/register/Register.jsx';
import {Profile} from '../pages/profile/Profile.jsx';
import {Settings} from '../pages/settings/Settings';
import {AdminPanel} from '../pages/admin/AdminPanel';
import {NotFound} from '../pages/notfound/NotFound.jsx';

/**
 * A comprehensive settings page component that allows users to manage their account
 * profile, application preferences, and data privacy.
 * <p>The component performs the following tasks:
 * <ul>
 * <li><b>Profile Management:</b> Fetches current user details and provides a form to update
 * the username, email, and password.</li>
 * <li><b>Map Preferences:</b> Allows users to configure a local proximity radius for
 * viewing reports, with settings persisted to localStorage.</li>
 * <li><b>Account Deletion:</b> Provides a secure, modal-confirmed pathway for permanent
 * account deletion.</li>
 * </ul>
 * </p>
 * @returns A JSX element containing the structured settings interface.
 */
const AppRoutes = ({ currentMode, setCurrentMode }) => {
    return (
        <Routes>
            < Route path = "/" element = {<Navigate to = "/map" replace/>} />
            < Route path = "/map" element = {<Map currentMode={currentMode} setCurrentMode = {setCurrentMode} />} />
            < Route path = "/profile" element = {<Profile/>} />
            < Route path = "/settings" element = {<Settings/>} />
            < Route path = "/login" element = {<Login/>} />
            < Route path = "/register" element = {<Register/>} />
            < Route path = "/admin" element = {<AdminPanel/>} />
            < Route path = "*" element = {<NotFound/>} />
        </Routes>
    );
}

export default AppRoutes;