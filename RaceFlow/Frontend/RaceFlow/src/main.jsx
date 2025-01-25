import React, { useEffect } from "react";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import HomePage from "./pages/HomePage";
import About from "./pages/About";
import DriverStandings from "./pages/DriverStandings";
import ConstructorStandings from "./pages/ConstructorStandings";
import DriverDetails from "./pages/DriverDetails";
import CarDetails from "./pages/CarDetails";
import LoginPage from "./pages/LoginPage";
import AdminPage from "./pages/AdminPage";

const App = () => {
    useEffect(() => {
    if (!sessionStorage.getItem("appInitialized")) {
        localStorage.removeItem("token");
        sessionStorage.setItem("appInitialized", "true");
    }
    }, []);

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={<HomePage />} />
                <Route path="/about" element={<About />} />
                <Route path="/driver-standings" element={<DriverStandings />} />
                <Route path="/constructor-standings" element={<ConstructorStandings />} />
                <Route path="/driver/:id" element={<DriverDetails />} />
                <Route path="/car/:id" element={<CarDetails />} />
                <Route
                    path="/admin"
                    element={
                                <AdminPage />
                    }
                />
            </Routes>
        </BrowserRouter>
    );
};

const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem("token");

    if (!token) {
        // Redireciona para a página de login se não houver token
        return <Navigate to="/login" />;
    }

    return children;
};

// Renderiza a aplicação
createRoot(document.getElementById("root")).render(
    <StrictMode>
        <App />
    </StrictMode>
);
