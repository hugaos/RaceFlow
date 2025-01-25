import React, { useState } from 'react';
import '../css/DriverModal.css'; // Usando o mesmo CSS do DriverModal

const RaceModal = ({ onClose, onSave }) => {
    const [raceName, setRaceName] = useState('');
    const [location, setLocation] = useState('');
    const [totalLaps, setTotalLaps] = useState('');
    const [trackTemperature, setTrackTemperature] = useState('');
    const [weather, setWeather] = useState('');

    const handleSave = async () => {
        // Monta o objeto apenas com os campos preenchidos
        const newRace = {};
        if (raceName) newRace.name = raceName;
        if (location) newRace.location = location;
        if (totalLaps) newRace.totalLaps = Number(totalLaps);
        if (trackTemperature) newRace.trackTemperature = Number(trackTemperature);
        if (weather) newRace.weather = weather;

        try {
            const response = await fetch("http://localhost:8080/api/races", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem('token')}`,
                },
                body: JSON.stringify(newRace),
            });

            if (response.ok) {
                const savedRace = await response.json().catch(() => null); // Handle empty response
                onSave(savedRace);
            } else {
                console.error("Failed to create race:", response.statusText);
            }
        } catch (error) {
            console.error("Error saving race:", error);
        }
    };

    return (
        <div className="modal">
            <div className="modal-content">
                <h2>Create Race</h2>
                <div className="form-group">
                    <label htmlFor="raceName">Race Name</label>
                    <input
                        type="text"
                        id="raceName"
                        value={raceName}
                        onChange={(e) => setRaceName(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="location">Location</label>
                    <input
                        type="text"
                        id="location"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="totalLaps">Total Laps</label>
                    <input
                        type="number"
                        id="totalLaps"
                        value={totalLaps}
                        onChange={(e) => setTotalLaps(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="trackTemperature">Track Temperature</label>
                    <input
                        type="number"
                        id="trackTemperature"
                        value={trackTemperature}
                        onChange={(e) => setTrackTemperature(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="weather">Weather</label>
                    <select
                        id="weather"
                        value={weather}
                        onChange={(e) => setWeather(e.target.value)}
                    >
                        <option value="">Select Weather</option>
                        <option value="sunny">Sunny</option>
                        <option value="rainy">Rainy</option>
                        <option value="foggy">Foggy</option>
                        <option value="windy">Windy</option>
                    </select>
                </div>
                <div className="modal-actions">
                    <button onClick={handleSave}>Save</button>
                    <button onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
};

export default RaceModal;
