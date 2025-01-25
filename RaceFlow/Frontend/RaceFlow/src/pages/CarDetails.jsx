import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Navbar from "../components/navbar";
import "../css/CarDetails.css";
import "../css/index.css";
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import DriverTable from "../components/driverTable";

const CarDetails = () => {
    const { id } = useParams();
    const [car, setCar] = useState(null);
    const [driver, setDriver] = useState(null);
    const [totalLaps, setTotalLaps] = useState(0);
    const [events, setEvents] = useState([]); // Fila de eventos


    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                // Fetch car details
                const carResponse = await fetch(`http://localhost:8080/api/cars/${id}`);
                if (carResponse.ok) {
                    const carData = await carResponse.json();
                    setCar(carData);

                } else {
                    console.error("Failed to fetch car details:", carResponse.statusText);
                }
    
                // Fetch race details to get total laps
                const raceResponse = await fetch("http://localhost:8080/api/races");
                if (raceResponse.ok) {
                    const races = await raceResponse.json();
                    if (races.length > 0) {
                        setTotalLaps(races[0].totalLaps);
                    } else {
                        console.warn("No races found");
                        setTotalLaps(0);
                    }
                } else {

                    console.error("Failed to fetch races:", raceResponse.statusText);
                }
    
                // Fetch driver details
                const driverResponse = await fetch(`http://localhost:8080/api/drivers/${id}`);
                if (driverResponse.ok) {
                    const driverData = await driverResponse.json();
                    setDriver(driverData);
                } else {
                    console.error("Failed to fetch driver:", driverResponse.statusText);
                }
            } catch (error) {
                console.error("Error fetching initial data:", error);
            }
        };
    
        fetchInitialData();
    
        const socket = new SockJS("http://localhost:8080/api/websocket");
        const stompClient = Stomp.over(() => socket);
    
        // Connect WebSocket
        stompClient.connect({}, () => {
            console.log("Connected to WebSocket");
    
            // Subscribe to car updates
            stompClient.subscribe("/topic/cars", (message) => {
                const carData = JSON.parse(message.body);
                console.log("Received car update:", carData);
    
                if (carData && carData.driverID === parseInt(id)) {
                    setCar((prevCar) => ({
                        ...prevCar,
                        ...carData,
                    }));
    
                    // Log currentSpeed for debugging
                    console.log("Updated currentSpeed:", carData.currentSpeed);
                }
            });
    
            // Subscribe to driver updates
            stompClient.subscribe("/topic/drivers", (message) => {
                const driverData = JSON.parse(message.body);
                console.log("Received driver update:", driverData);
    
                if (driverData && driverData.id === parseInt(id)) {
                    setDriver((prevDriver) => ({
                        ...prevDriver,
                        ...driverData,

                }));
              };
            });
            
    
            // Subscribe to race updates (total laps)
            stompClient.subscribe("/topic/races", (message) => {
                const raceData = JSON.parse(message.body);
                console.log("Received race update:", raceData);
    
                if (raceData && raceData.totalLaps) {
                    setTotalLaps(raceData.totalLaps);
                }
            });

            stompClient.subscribe("/topic/race", (message) => {
                const raceData = JSON.parse(message.body);
                if (raceData.currentLap > currentLap_) {
                  setCurrentLap(raceData.currentLap);
                  currentLap_ = raceData.currentLap;
                }
      
                // Atualiza weather e trackTemperature se presentes no raceData
                if (raceData.weather) {
                  setWeather(raceData.weather);
                }
                if (raceData.trackTemperature !== undefined && raceData.trackTemperature !== null) {
                  setTrackTemperature(raceData.trackTemperature);
                }
              });
      
              stompClient.subscribe("/topic/pitstop", (message) => {
                const pitstopData = JSON.parse(message.body);
                console.log("PITSTOP Received WebSocket update:", pitstopData);
              
                const formattedDuration = formatDuration(pitstopData.duration);
              
                updateEvents({
                  id: Date.now(),
                  iconClass: "event-icon pitstop",
                  title: "Pit Stop",
                  details: `Driver ${pitstopData.driver.name || "Unknown"} (${pitstopData.driver.driverNumber}), Duration ${formattedDuration}, Lap ${pitstopData.lap || "?"}, Tyre: ${pitstopData.new_tyre || "Unknown"}`
                });
              });
              
              // Função para formatar a duração ISO 8601 (PT28S -> 28s)
              function formatDuration(duration) {
                if (!duration) return "Unknown";
                const match = duration.match(/PT(\d+)S/);
                return match ? `${match[1]}s` : duration;
              }
        });
    
        return () => {
            stompClient.disconnect(() => {
                console.log("Disconnected from WebSocket");
            });
        };
    }, [id]);
    


    if (!car) {
        return <div>Loading car details...</div>;
    }

    console.log("Car details:", car);

    return (
        <div className="page">
            <Navbar />
            <div className="container">
                <div className="row">
                    <div className="table-column">
                        <DriverTable />
                    </div>
                    <div className="track-col">
                        <div className="quadrado">
                            <div
                                className="pilot-name-container"
                                style={{ backgroundColor: car.teamColor || "#d32f2f" }}
                            >
                                <div className="pilot-number">{car.driver.driverNumber}</div>
                                <div className="pilot-name">
                                    <h1>{car.driver.name}</h1>
                                    <h3>{car.teamName}</h3>
                                </div>
                            </div>
                            <div className="progress-bar-container">
                                <div className="progress-bar">
                                    <div
                                        className="progress"
                                        style={{
                                            width: `${(car.currentLap / totalLaps) * 100}%`,
                                            backgroundColor: `#${car.teamColor || "d32f2f"}`,
                                        }}
                                    ></div>
                                    <div
                                        className="progress-marker"
                                        style={{
                                            left: `calc(${(car.currentLap / totalLaps) * 100}% - 15px)`,
                                        }}
                                    >
                                        {car.currentLap}
                                    </div>
                                </div>
                                <div className="progress-bar-markers">
                                    <span className="start-marker">0</span>
                                    <span className="end-marker">🏁</span>
                                </div>
                            </div>
                            <div className="car-image-wrapper">
                                <div className="tyre-temp top-left">
                                    {car.tyreTemp && car.tyreTemp[0]
                                        ? `${car.tyreTemp[0].toFixed(2)}°`
                                        : "N/A"}
                                </div>
                                <div className="tyre-temp top-right">
                                    {car.tyreTemp && car.tyreTemp[1]
                                        ? `${car.tyreTemp[1].toFixed(2)}°`
                                        : "N/A"}
                                </div>
                                <img
                                    src={`http://localhost:8080${car.image_url}`}
                                    alt="Car Image"
                                    className="car-image"
                                />
                                <div className="tyre-temp bottom-left">
                                    {car.tyreTemp && car.tyreTemp[2]
                                        ? `${car.tyreTemp[2].toFixed(2)}°`
                                        : "N/A"}
                                </div>
                                <div className="tyre-temp bottom-right">
                                    {car.tyreTemp && car.tyreTemp[3]
                                        ? `${car.tyreTemp[3].toFixed(2)}°`
                                        : "N/A"}
                                </div>
                                {/* <div
                                    className="bpm-bubble"
                                    style={{ backgroundColor: car.teamColor || "#d32f2f" }}
                                >
                                    {driver?.heartRate || "N/A"} bpm
                                </div> */}
                            </div>
                            <div className="info-column">
                                <div className="top-info">
                                    <div className="car-stats">
                                        <div
                                            style={{ backgroundColor: `#${car.teamColor || "d32f2f"}` }}
                                        >
                                            <h4>Lap</h4>
                                        </div>
                                        <p>
                                            {car.currentLap} / {totalLaps}
                                        </p>
                                    </div>
                                </div>
                                <div className="details-panel">
                                    <h3>Details</h3>
                                    <div className="details-item">
                                        <span className="detail-icon">⏱</span>
                                        <p>
                                            {car.currentSpeed.toFixed(2) || "N/A"} Km/h
                                        </p>
                                    </div>
                                    <div className="details-item">
                                        <span className="detail-icon">⚙️</span>
                                        <p>{car.gear || "N/A"} Gear</p>
                                    </div>
                                    <div className="details-item">
                                        <span className="detail-icon">🔧</span>
                                        <p>{car.rpm || "N/A"} rpm</p>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CarDetails;
