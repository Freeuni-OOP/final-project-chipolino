import {createContext, useState, useEffect, useCallback} from "react";
import {getCurrentUser} from "../api/userApi.js";
import {login, logout, register} from "../api/authApi.js";

/**
 * Global Context object for sharing authentication state across the application.
 */
export const AuthContext = createContext(undefined)

/**
 * Context Provider component that manages global user authentication state.
 * Wraps the application to provide user data, loading states, and auth handlers.
 */
export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true);

    const fetchUser = useCallback(() => {
        getCurrentUser().
        then(res => setUser(res)).
        catch(() => setUser(null)).
        finally(() => setLoading(false))
    }, []);

    useEffect(()=> {fetchUser()},
        [fetchUser])

    const handleLogin =  async (data) => {
        await login(data)
        const res = await getCurrentUser()
        setUser(res)
    }

    const handleLogout =  async () => {
        await logout()
        setUser(null)
    }

    const handleRegister =  async (data) => {
        try {
            await register(data)
            const res = await getCurrentUser()
            setUser(res)
        }
        catch(err){
            throw err;
        }
    }

    return (
        <AuthContext.Provider
            value={{user, loading,handleLogin, handleRegister, handleLogout}}>
            {children}
        </AuthContext.Provider>
    )
}