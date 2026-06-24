import { Routes, Route, Navigate } from 'react-router-dom';

import Map from '../pages/map/Map';
import Login from '../pages/login/Login';
import Register from '../pages/register/Register';
import Profile from '../pages/profile/Profile';
import Settings from '../pages/settings/Settings';
import AdminPanel from '../pages/admin/AdminPanel';
import NotFound from '../pages/notfound/NotFound';

const AppRoutes = () => {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/map" replace/>} />
            <Route path="/map" element={<Map/>} />
            <Route path="/profile" element={<Profile/>} />
            <Route path="/settings" element={<Settings/>} />
            <Route path="/login" element={<Login/>} />
            <Route path="/register" element={<Register/>} />
            <Route path="/admin" element={<AdminPanel/>} />
            <Route path="*" element={<NotFound/>} />
        </Routes>
    );
}

export default AppRoutes;