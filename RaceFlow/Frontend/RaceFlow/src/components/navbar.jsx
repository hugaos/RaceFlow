import React from "react";
import { NavLink, Link } from "react-router-dom";
import "../css/navbar.css";

function Navbar({isAdminPage}) {
    return (
        <nav className="navbar">
            <div className="nav-inner">
                {/* Logo da RaceFlow */}
                <Link to="/" className="logo-container">
                    <div className="logo-icon"></div>
                    <div className="logo-text">🏎 RaceFlow</div>
                </Link>

                <div className="nav-links">
                    <NavLink
                        to="/driver-standings"
                        className={({ isActive }) => (isActive ? "active-link" : "")}
                    >
                        Driver Standings
                    </NavLink>
                    <NavLink
                        to="/constructor-standings"
                        className={({ isActive }) => (isActive ? "active-link" : "")}
                    >
                        Constructor Standings
                    </NavLink>
                    {/* Botão para Login */}
                    <NavLink
                        to="/login"
                        className="login-button"
                    >
                        {isAdminPage ? "Logout" : "Login"}
                    </NavLink>
                </div>
            </div>
        </nav>
    );
}

export default Navbar;
