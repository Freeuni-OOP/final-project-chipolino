import {useContext} from "react"
import {AuthContext} from "../context/AuthContext.jsx"

/**
 * Custom hook to provide access to authentication context.
 * @returns {{
 * user: Object|null,
 * loading: boolean,
 * handleLogin: function,
 * handleRegister: function,
 * handleLogout: function
 * }}
 * @throws {Error} If the hook is used outside a {@link AuthProvider} component tree.
 */
export const useAuth = () => {
    const context = useContext(AuthContext)

    if(context === undefined){
        throw new Error("Context is not defined")
    }
    return context
}