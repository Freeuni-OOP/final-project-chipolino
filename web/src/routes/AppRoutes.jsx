import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import Map from '../pages/Map';
import Login from '../pages/Login';
import Register from '../pages/Register';
import Profile from '../pages/Profile';
import Settings from '../pages/Settings';
import AdminPanel from '../pages/AdminPanel';
import NotFound from '../pages/NotFound';
import axiosApi from "../api/axiosClient.js";

/**
 * appRoutes function sets up the main map for the app's pages.
 * It connects web addresses (URL-s) to the actual screens the user sees.
 * @returns {JSX.Element} The whole navigation system so the app can use it.
 */
const AppRoutes = () => {
    return (
      <BrowserRouter>
          <Routes>
              < Route path = "/" element = {<Navigate to = "/map" replace/>} />
              < Route path = "/map" element = {<Map/>} />
              < Route path = "/profile" element = {<Profile/>} />
              < Route path = "/settings" element = {<Settings/>} />
              < Route path = "/login" element = {<Login/>} />
              < Route path = "/register" element = {<Register/>} />
              < Route path = "/admin" element = {<AdminPanel/>} />
              < Route path = "*" element = {<NotFound/>} />
          </Routes>
      </BrowserRouter>
    );
}

export default AppRoutes;