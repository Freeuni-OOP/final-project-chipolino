import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Map from '../pages/map/Map.jsx';
import Login from '../pages/login/Login.jsx';
import Register from '../pages/register/Register.jsx';
import {Profile} from '../pages/profile/Profile.jsx';
import {Settings} from '../pages/settings/Settings';
import {AdminPanel} from '../pages/admin/AdminPanel';
import {NotFound} from '../pages/notfound/NotFound.jsx';
import {ProtectedRoute} from "./ProtectedRoute.jsx";
import {Verify} from "../pages/verify/Verify.jsx";

/**
 * AppRoutes function sets up the main map for the app's pages.
 * @returns {JSX.Element} The whole navigation system to use it.
 */
const AppRoutes = ({ currentMode, setCurrentMode, followUser, setFollowUser }) => {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/map" replace />} />
            <Route path="/map" element={<Map currentMode={currentMode}
                                             setCurrentMode={setCurrentMode}
                                             followUser={followUser}
                                             setFollowUser={setFollowUser}
                                        />}
            />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/profile" element={
                <ProtectedRoute>
                    <Profile isMe={true} />
                </ProtectedRoute>
            } />
            <Route path="/settings" element={
                <ProtectedRoute>
                    <Settings />
                </ProtectedRoute>
            } />
            <Route path="/admin" element={
                <ProtectedRoute role={'ADMIN'}>
                    <AdminPanel />
                </ProtectedRoute>
            } />
            <Route path="/users/:id" element={<Profile isMe={false} />} />
            <Route path="/verify" element={<Verify />} />
            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}

export default AppRoutes;