import React, { useEffect, useState } from "react";
import "../css/DriverStandings.css";
import Navbar from "../components/navbar.jsx";
import { Link } from "react-router-dom";

const DriverStandings = () => {
  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDrivers = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/drivers", {
          method: "GET",
        });

        if (response.ok) {
          const data = await response.json();
          console.log("Dados recebidos do backend:", data);
          setDrivers(data);
        } else {
          console.error("Erro na resposta da API:", response.statusText);
        }
      } catch (error) {
        console.error("Erro ao buscar os drivers:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDrivers();
  }, []);

  if (loading) {
    return (
        <div className="loading-page">
          <h1>Loading...</h1>
        </div>
    );
  }


  return (
      <div className="driver-standings-page">
        <Navbar />
        <div className="driver-standings-container">
          <h1 className="header">Driver Standings</h1>
          <table className="driver-standings-table">
            <thead>
            <tr>
              <th>#</th>
              <th>Driver</th>
              <th>Team</th>
              <th>Nationality</th>
              <th>Points</th>
            </tr>
            </thead>
            <tbody>
            {drivers.map((driver, index) => (
                <tr key={driver.id}>
                  <td>{index + 1}</td>
                  <td className="driver-cell">
                    <span
                        className="team-colour-indicator"
                        style={{backgroundColor: `#${driver.teamColour}` || "#444444"}}// Cor da equipa ou padrão
                    ></span>
                    <Link to={`/driver/${driver.id}`}>
                      {driver.name.toUpperCase()}
                    </Link>
                  </td>
                  <td>{driver.teamName}</td>
                  <td>{driver.countryCode}</td>
                  <td>0</td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
      </div>
  );
};

export default DriverStandings;
