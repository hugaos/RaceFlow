import React, { useState, useEffect } from 'react';
import '../css/DriverModal.css';

const DriverModal = ({ isEditMode, driver, onClose, onSave }) => {
    const [driverName, setDriverName] = useState(isEditMode ? driver.name : '');
    const [driverNumber, setDriverNumber] = useState(isEditMode ? driver.driverNumber : '');
    const [teamName, setTeamName] = useState(isEditMode ? driver.teamName : '');
    const [countryCode, setCountryCode] = useState(isEditMode ? driver.countryCode : '');
    const [acronym, setAcronym] = useState(isEditMode ? driver.acronym : '');
    const [teamColour, setTeamColour] = useState(isEditMode ? driver.teamColour : '');
    const [teams, setTeams] = useState([]); // Equipas
    const [drivers, setDrivers] = useState([]); // Lista de drivers
    const [error, setError] = useState(''); // Mensagem de erro
    const token = localStorage.getItem('token');

    // Buscar a lista de drivers e extrair as equipas
    useEffect(() => {
        const fetchDrivers = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/drivers", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (response.ok) {
                    const data = await response.json();
                    setDrivers(data); // Guardar a lista de drivers
                    const teamsList = [...new Set(data.map(driver => driver.teamName))]; // Extrair equipas únicas
                    setTeams(teamsList);
                } else {
                    console.error("Error fetching drivers:", response.statusText);
                }
            } catch (error) {
                console.error("Error fetching drivers:", error);
            }
        };

        fetchDrivers();
    }, [token]);

    // Atualizar a cor da equipa ao selecionar uma equipa
    useEffect(() => {
        const selectedTeam = drivers.find(driver => driver.teamName === teamName);
        if (selectedTeam) {
            setTeamColour(selectedTeam.teamColour);
        }
    }, [teamName, drivers]);

    // Verificar se o número do driver já existe
    const validateDriverNumber = () => {
        const existingDriver = drivers.find(driver => 
            driver.driverNumber === parseInt(driverNumber) && 
            (!isEditMode || driver.id !== driver.id) // Ignorar o próprio driver ao editar
        );

        if (existingDriver) {
            setError(`Driver number ${driverNumber} is already in use by ${existingDriver.name}.`);
        } else {
            setError('');
        }
    };
    
    const addDriverToRace = async (driverId) => {
    if (currentRace) {
        try {
            // Cria o novo carro associado ao driver
            const newCar = {
                driver: { id: driverId }, // Apenas o ID do driver é necessário
            };

            // Adiciona o novo carro à lista de carros da corrida
            const updatedRace = {
                ...currentRace,
                cars: [...currentRace.cars, newCar],
            };

            const response = await fetch(`http://localhost:8080/api/races/${currentRace.id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(updatedRace),
            });

            if (response.ok) {
                const raceData = await response.json();
                setCurrentRace(raceData);
                console.log("Driver added to race:", raceData);
            } else {
                console.error("Failed to add driver to race:", response.statusText);
            }
        } catch (error) {
            console.error("Error adding driver to race:", error);
        }
    }
};

    const handleSave = async () => {
        const newDriver = {
            name: driverName,
            driverNumber: driverNumber,
            teamName: teamName,
            countryCode: countryCode,
            acronym: acronym,
            teamColour: teamColour,
        };

        try {
            const url = isEditMode
                ? `http://localhost:8080/api/drivers/${driver.id}` // Para edição, inclui o ID na URL
                : "http://localhost:8080/api/drivers/"; // Para criação, usa apenas a rota base

            const response = await fetch(url, {
                method: isEditMode ? "PUT" : "POST", // Escolhe o método com base no modo
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`, // Adiciona o token para autenticação
                },
                body: JSON.stringify(newDriver), // Envia os dados do driver no corpo da requisição
            });

            if (response.ok) {
                const data = await response.json();
                onSave(data); // Atualiza a lista de drivers no frontend
            } else {
                console.error("Failed to save driver:", response.statusText);
            }
        } catch (error) {
            console.error("Error saving driver:", error);
        }
    };

    return (
        <div className="modal">
            <div className="modal-content">
                <h2>{isEditMode ? "Edit Driver" : "Create Driver"}</h2>
                {error && <div className="error-message">{error}</div>}
                <div className="form-group">
                    <label htmlFor="driverName">Driver Name</label>
                    <input
                        type="text"
                        id="driverName"
                        value={driverName}
                        onChange={(e) => setDriverName(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="driverNumber">Driver Number</label>
                    <input
                        type="number"
                        id="driverNumber"
                        value={driverNumber}
                        onChange={(e) => setDriverNumber(e.target.value)}
                        onBlur={validateDriverNumber} // Verifica ao sair do campo
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="teamName">Team</label>
                    <select
                        id="teamName"
                        value={teamName}
                        onChange={(e) => setTeamName(e.target.value)}
                        required
                    >
                        <option value="">Select a team</option>
                        {teams.map((team, index) => (
                            <option key={index} value={team}>
                                {team}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="countryCode">Country Code</label>
                    <input
                        type="text"
                        id="countryCode"
                        value={countryCode}
                        onChange={(e) => setCountryCode(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="acronym">Acronym</label>
                    <input
                        type="text"
                        id="acronym"
                        value={acronym}
                        onChange={(e) => setAcronym(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="teamColour">Team Colour</label>
                    <input
                        type="text"
                        id="teamColour"
                        value={teamColour}
                        onChange={(e) => setTeamColour(e.target.value)}
                        required
                        disabled // Desabilitado para evitar edição manual
                    />
                </div>
                <div className="modal-actions">
                    <button onClick={handleSave}>{isEditMode ? "Save Changes" : "Create Driver"}</button>
                    <button onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
};

export default DriverModal;
