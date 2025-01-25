import React, { useState, useEffect } from 'react';
import Navbar from '../components/navbar';
import F1Track from '../components/F1track';
import DriverTable from '../components/driverTable';
import DriverModal from '../components/DriverModal'; // Modal para criar/editar drivers
import RaceModal from '../components/RaceModal'; // Modal para criar corridas
import '../css/index.css';
import '../css/AdminPage.css';

const AdminPage = () => {
    const [drivers, setDrivers] = useState([]);
    const [selectedDriverId, setSelectedDriverId] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [isEditMode, setIsEditMode] = useState(false);
    const [showRaceControl, setShowRaceControl] = useState(false);
    const [showDriversControl, setShowDriversControl] = useState(false);
    const [showRaceModal, setShowRaceModal] = useState(false);
    const [currentRace, setCurrentRace] = useState(null);
    const token = localStorage.getItem('token'); // Recupera o token do localStorage
    const [driverTableKey, setDriverTableKey] = useState(0);

    // Fetch inicial de drivers
    useEffect(() => {
        const fetchDrivers = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/drivers", {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (response.ok) {
                    const driversData = await response.json();
                    setDrivers(driversData);
                } else {
                    console.error("Error fetching drivers:", response.statusText);
                }
            } catch (error) {
                console.error("Error fetching drivers:", error);
            }
        };
        const fetchRace = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/races", {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (response.ok) {
                    const racesData = await response.json();
                    if (racesData.length > 0) {
                        console.log("Fetched Race:", racesData[0]);
                        setCurrentRace(racesData[0]); // Configura a primeira corrida
                    } else {
                        setCurrentRace(null); // Nenhuma corrida disponível
                    }
                } else {
                    console.error("Error fetching race:", response.statusText);
                    setCurrentRace(null);
                }
            } catch (error) {
                console.error("Error fetching race:", error);
                setCurrentRace(null);
            }
        };

        fetchRace();
        fetchDrivers();
    }, [token]);

    const handleCreateRace = () => {
        setShowRaceModal(true);
    };
    const handleCreateDriver = () => {
        if (currentRace) {
            alert("You cannot create a driver while a race is active.");
            return;
        }
        setIsEditMode(false);
        setShowModal(true);
    };

    const handleEditDriver = () => {
        if (selectedDriverId) {
            setIsEditMode(true);
            setShowModal(true);
        } else {
            console.error("No driver selected to edit");
        }
    };

    const handleDeleteDriver = async () => {
        if (currentRace) {
            alert("You cannot create a driver while a race is active.");
            return; 
        }
            if (selectedDriverId) {
            try {
                const response = await fetch(`http://localhost:8080/api/drivers/${selectedDriverId}`, {
                    method: "DELETE",
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (response.ok) {
                    setDrivers((prevDrivers) => prevDrivers.filter((driver) => driver.id !== selectedDriverId));
                    setSelectedDriverId(null);
                } else {
                    console.error("Error deleting driver:", response.statusText);
                }
            } catch (error) {
                console.error("Error deleting driver:", error);
            }
        }
    };

    const handleCreateAllDrivers = async () => {
        try {
            const response = await fetch("http://localhost:8080/api/drivers/createAll", {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`, // Apenas cabeçalho de autorização
            },
            });

            if (!response.ok) {
            console.error("Error creating all drivers:", response.status, response.statusText);
            return;
            }

            console.log("Drivers created successfully");
                setDriverTableKey((prevKey) => prevKey + 1); // Atualiza a key
        } catch (error) {
            console.error("Error creating all drivers:", error);
        }
    };

    const startRace = async () => {
        if (!currentRace) {
            console.error("No race selected to start.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/races/${currentRace.id}/start`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({}),  // Sending an empty body (equivalent to sending `{}` as JSON)
            });
    
            if (response.ok) {
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    const data = await response.json();
                    console.log("Race started successfully:", data);
                } else {
                    const text = await response.text();
                    console.log("Race started successfully:", text);
                }
            } else {
                console.error("Error starting race:", response.statusText);
            }
        } catch (error) {
            console.error("Error starting race:", error);
        }
    };

    const stopRace = async () => {
        const raceId = 1;  // You can dynamically set this
        try {
            const response = await fetch(`http://localhost:8080/api/races/${raceId}/stop`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({}),  // Sending an empty body (equivalent to sending `{}` as JSON)
            });

            if (response.ok) {
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    const data = await response.json();
                } else {
                    const text = await response.text();
                    console.log("Race stopped successfully:", text);
                }
            } else {
                console.error("Error stopping race:", response.statusText);
            }
        }
        catch (error) {
            console.error("Error stopping race:", error);
        }
    };


    const selectedDriver = selectedDriverId ? drivers.find((d) => d.id === selectedDriverId) : null;

    return (
        <div className="page">
            <Navbar isAdminPage={true}/>
            <div className="admin-controls">
                <h2>Admin Controls</h2>
                <div className="button-group">
                    <button onClick={() => setShowDriversControl(!showDriversControl)}>Toggle Drivers Control</button>
                    <button onClick={() => setShowRaceControl(!showRaceControl)}>Toggle Race Control</button>
                </div>
                <div className="control-panel">
                    {showDriversControl && (
                        <div className="control-section">
                            <h3>Manage Drivers</h3>
                            <div className="control-content">
                                <select
                                    value={selectedDriverId || ""}
                                    onChange={(e) => {
                                        const driverId = e.target.value ? Number(e.target.value) : null;
                                        setSelectedDriverId(driverId);
                                    }}
                                >
                                    <option value="">Select a driver</option>
                                    {drivers.map((driver) => (
                                        <option key={driver.id} value={driver.id}>
                                            {driver.name || `Driver #${driver.id}`}
                                        </option>
                                    ))}
                                </select>
                                <div className="button-group">
                                    <button onClick={handleCreateDriver}>Create Driver</button>
                                    <button onClick={handleEditDriver}>Edit Driver</button>
                                    <button onClick={handleDeleteDriver}>Delete Driver</button>
                                    <button onClick={handleCreateAllDrivers}>Create All Drivers</button>
                                </div>
                            </div>
                        </div>
                    )}

                    {showRaceControl && (
                        <div className="control-section">
                            <h3>Race Controls</h3>
                            <div className="control-content">
                                {currentRace ? (
                                    <>
                                        <p>Current Race: {currentRace.name}</p>
                                        <div className="button-group">
                                            <button onClick={() => startRace()}>Start Race</button>
                                            <button onClick={() => stopRace()}>Stop Race</button>
                                            <button
                                                onClick={async () => {
                                                    try {
                                                        const response = await fetch(`http://localhost:8080/api/races/${currentRace.id}`, {
                                                            method: "DELETE",
                                                            headers: { Authorization: `Bearer ${token}` },
                                                        });
                                                        if (response.ok) {
                                                            console.log("Race deleted");
                                                            setCurrentRace(null);
                                                        } else {
                                                            console.error("Failed to delete race:", response.statusText);
                                                        }
                                                    } catch (error) {
                                                        console.error("Error deleting race:", error);
                                                    }
                                                }}
                                            >
                                                Delete Race
                                            </button>
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        <p>No race created</p>
                                        <button onClick={handleCreateRace}>Create Race</button>
                                    </>
                                )}
                            </div>
                        </div>
                    )}

                </div>
            </div>

            {/* Driver Modal */}
            {showModal && (
                <DriverModal
                    isEditMode={isEditMode}
                    driver={isEditMode ? selectedDriver : null}
                    onClose={() => setShowModal(false)}
                    onSave={(newDriver) => {
                        if (isEditMode) {
                            setDrivers((prev) =>
                                prev.map((driver) =>
                                    driver.id === newDriver.id ? newDriver : driver
                                )
                            );
                        } else {
                            setDrivers((prev) => [...prev, newDriver]);
                            addDriverToRace(newDriver.id);
                        }
                        setShowModal(false);
                    }}
                />
            )}

            {/* Race Modal */}
            {showRaceModal && (
                <RaceModal
                    onClose={() => setShowRaceModal(false)}
                    onSave={(newRace) => {
                        setCurrentRace(newRace);
                        setShowRaceModal(false);
                    }}
                />
            )}
            <div className="container">
                <div className="row">
                    <div className="table-column">
                        <DriverTable key={driverTableKey} />
                    </div>
                    <div className="track-col">
                        <F1Track />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminPage;
