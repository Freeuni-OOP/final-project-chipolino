import {useAuth} from "../hooks/useAuth.js";
import {Navigate} from "react-router-dom";
import {Spinner} from "../components/common/spinner/Spinner.jsx";

/**
 * Checks the authentication status and roles of current user.
 * @param children - The protected content to render if access is granted.
 * @param role - Optional, specific role required to access this route.
 * @returns Children component if satisfies requirements,
 * or a Navigate component to redirect the user.
 */
export const ProtectedRoute = ({children, role}) => {
    const { user, loading } = useAuth();

    if (loading) {
        return <Spinner fullScreen />;
    }

    if(!user){
        return <Navigate to={"/login"}/>;
    }

    if(role && user.role !== role){
        return <Navigate to="/" replace />;
    }

    return children;
}