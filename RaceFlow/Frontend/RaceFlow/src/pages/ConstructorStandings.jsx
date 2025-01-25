import React, { useEffect, useState } from "react";
import "../css/ConstructorStandings.css";
import Navbar from "../components/navbar.jsx";
const ConstructorStandings = () => {
  const [constructors, setConstructors] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDriversAndCalculateConstructors = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/drivers", {
          method: "GET",
        });

        if (response.ok) {
          const drivers = await response.json();
          console.log("Drivers fetched for constructor standings:", drivers);

          const constructorPoints = drivers.reduce((acc, driver) => {
            const team = driver.teamName;
            const teamColour = driver.teamColour;
            if (!acc[team]) {
              acc[team] = { teamName: team, countryCode: driver.countryCode, points: 0, teamColour: teamColour };
            }
            acc[team].points += driver.racePoints || driver.id; //! compor isto 
            return acc;
          }, {});

          // Convert object to array and sort by points
          const sortedConstructors = Object.values(constructorPoints).sort(
            (a, b) => b.points - a.points
          );

          setConstructors(sortedConstructors);
        } else {
          console.error("Erro na resposta da API:", response.statusText);
        }
      } catch (error) {
        console.error("Erro ao buscar os drivers:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDriversAndCalculateConstructors();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
      <div className={"constructor-standings-page"}>

        <div className="constructor-standings-container">
          <Navbar />

          <h1>Constructor Standings</h1>
          <table className="constructor-standings-table">
            <thead>
              <tr>
                <th>Pos</th>
                <th>Team</th>
                <th>Nationality</th>
                <th>Points</th>
              </tr>
            </thead>
            <tbody>
              {constructors.map((constructor, index) => (
                <tr key={constructor.teamName}>
                  <td>{index + 1}</td>
                  <td>
                    <span className="team-color" style={{ backgroundColor: constructor.teamColour }}></span>
                    {constructor.teamName}
                  </td>
                  <td>{constructor.countryCode}</td>
                  <td>{constructor.points}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
  );
};

export default ConstructorStandings;
