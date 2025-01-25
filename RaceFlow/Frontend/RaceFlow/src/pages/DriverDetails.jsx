import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Navbar from "../components/navbar";
import { Line } from "react-chartjs-2"; // Importing Chart.js Line component
import {
    Chart as ChartJS,
    LineElement,
    CategoryScale,
    LinearScale,
    PointElement,
    Tooltip,
    Legend,
} from "chart.js"; // Import necessary parts of Chart.js
import "../css/DriverDetails.css";
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";

// Register required Chart.js components
ChartJS.register(LineElement, CategoryScale, LinearScale, PointElement, Tooltip, Legend);

const DriverDetails = () => {
    const { id } = useParams(); // Driver ID from URL
    const [driver, setDriver] = useState(null);
    const [lapData, setLapData] = useState([]); // State to store lap data
    const [drivers, setDrivers] = useState([]);
    const [heartRate, setHeartRate] = useState(null);
    const [averageVelocity, setAverageVelocity] = useState(null);
    const [maxVelocity, setMaxVelocity] = useState(null);
    const [heartRateData, setHeartRateData] = useState([]); // State to store heart rate data over time

    // Fetch driver details
    useEffect(() => {
        const fetchDriverDetails = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/drivers/${id}`);
                if (response.ok) {
                    const data = await response.json();
                    setDriver({ ...data, teamColour: `#${data.teamColour}` });
                } else {
                    console.error("Error fetching driver details:", response.statusText);
                }
            } catch (error) {
                console.error("Error:", error);
            }
        };

        fetchDriverDetails();
    }, [id]);

    // Fetch lap data from car endpoint
    useEffect(() => {
        const fetchLapData = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/cars?driverId=${id}`);
                if (response.ok) {
                    const data = await response.json();
                    setLapData(data.lapTimes || []);
                } else {
                    console.error("Error fetching lap data:", response.statusText);
                }
            } catch (error) {
                console.error("Error fetching lap data:", error);
            }
        };

        fetchLapData();
    }, [id]);

    useEffect(() => {
        const socket = new SockJS("http://localhost:8080/api/websocket");
        const stompClient = Stomp.over(socket);

        stompClient.connect(
            {},
            () => {
                console.log("Connected to WebSocket");

                // Listener for combined driver and velocityInfo data
                stompClient.subscribe("/topic/drivers", (message) => {
                    const data = JSON.parse(message.body);
                    const driver = data.driver;
                    const velocityInfo = data.velocityInfo;

                    if (velocityInfo.driverID === Number(id)) {
                        setAverageVelocity(velocityInfo.averageVelocity);
                        setMaxVelocity(velocityInfo.maxVelocity);
                        setHeartRate(driver.heartRate);

                        // Update heart rate data with timestamp
                        setHeartRateData((prevData) => [
                            ...prevData,
                            { time: new Date().toLocaleTimeString(), heartRate: driver.heartRate },
                        ]);
                    }
                    setDrivers((prevDrivers) => {
                        const existingIndex = prevDrivers.findIndex((d) => d.id === driver.id);
                        if (existingIndex !== -1) {
                            // Update the existing driver data
                            return prevDrivers.map((d, index) =>
                                index === existingIndex
                                    ? { ...d, ...driver, ...velocityInfo }
                                    : d
                            );
                        } else {
                            // Add new driver to the list
                            return [...prevDrivers, { ...driver, ...velocityInfo }];
                        }
                    });
                });
            },
            (error) => {
                console.error("WebSocket connection error:", error);
            }
        );

        return () => {
            if (stompClient.connected) {
                stompClient.disconnect(() => {
                    console.log("WebSocket disconnected");
                });
            }
        };
    }, [id]);

    useEffect(() => {
        const interval = setInterval(() => {
            setHeartRateData((prevData) => {
                const newData = [...prevData];
                return newData.length > 50 ? newData.slice(newData.length - 50) : newData;
            });
        }, 3000);

        return () => clearInterval(interval);
    }, []);

    // Calculate average and fastest lap times
    const averageLapTime =
        lapData.length > 0
            ? (lapData.reduce((sum, lap) => sum + lap, 0) / lapData.length).toFixed(2)
            : "N/A";
    const fastestLapTime =
        lapData.length > 0 ? Math.min(...lapData).toFixed(2) : "N/A";

    // Prepare data for the lap times graph
    const lapChartData = {
        labels: lapData.map((_, index) => `Lap ${index + 1}`), // X-axis labels (Lap numbers), index + 1 for correct lap numbers
        datasets: [
            {
                label: "Lap Times",
                data: lapData.map((lap) => lap), // Y-axis data (Lap times)
                borderColor: driver ? driver.teamColour : "#000", // Use driver's team color for the line
                backgroundColor: "rgba(0, 0, 0, 0.1)",
                pointBackgroundColor: driver ? driver.teamColour : "#000",
                pointBorderColor: "#fff",
                tension: 0.4, // Smooth curve
            },
        ],
    };

    const chartOptions = {
        responsive: true,
        plugins: {
            legend: {
                display: false, // Hide legend
            },
            tooltip: {
                callbacks: {
                    label: (context) =>
                        `Lap Time: ${context.raw.toFixed(2)}s`, // Format tooltip data
                },
            },
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: "Laps",
                },
            },
            y: {
                title: {
                    display: true,
                    text: "Lap Time (s)",
                },
                ticks: {
                    callback: (value) => `${value}s`, // Add "s" to y-axis values
                },
            },
        },
    };

    // Prepare data for the heart rate graph
    const heartRateChartData = {
        labels: heartRateData.map((data) => data.time), // X-axis labels (time)
        datasets: [
            {
                label: "Heart Rate",
                data: heartRateData.map((data) => data.heartRate), // Y-axis data (heart rate)
                borderColor: driver ? driver.teamColour : "#000", // Use driver's team color for the line
                backgroundColor: "rgba(0, 0, 0, 0.1)",
                pointBackgroundColor: driver ? driver.teamColour : "#000",
                pointBorderColor: "#fff",
                tension: 0.4, // Smooth curve
            },
        ],
    };

    const heartRateChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                display: false, // Hide legend
            },
            tooltip: {
                callbacks: {
                    label: (context) =>
                        `Heart Rate: ${context.raw} bpm`, // Format tooltip data
                },
            },
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: "Time",
                },
            },
            y: {
                title: {
                    display: true,
                    text: "Heart Rate (bpm)",
                },
                ticks: {
                    callback: (value) => `${value} bpm`, // Add "bpm" to y-axis values
                },
            },
        },
    };

    if (!driver) {
        return <div>Loading driver details...</div>;
    }

    return (
        <div className="driver-details-page">
            <Navbar />
            <div className="driver-details-container">
                <div className="driver-header">
                    <img
                        style={{ borderColor: driver.teamColour }}
                        src={driver.photo && driver.photo !== "null" ? driver.photo : "/default_driver.jpg"}
                        className="driver-photo"
                        alt={`${driver.name}'s Photo`}
                    />
                    <div className="driver-info">
                        <h1 style={{ color: driver.teamColour }}>
                            {driver.driverNumber} - {driver.name}
                        </h1>
                        <h3 style={{ color: driver.teamColour }}>({driver.acronym})</h3>
                    </div>
                </div>

                <div className="driver-body">
                    <div className="details-left">
                        <table>
                            <tbody>
                                <tr>
                                    <td style={{ color: driver.teamColour }}>Average Velocity (km/h)</td>
                                    <td>
                                        {averageVelocity || "N/A"} km/h
                                    </td>
                                </tr>
                                <tr>
                                    <td style={{ color: driver.teamColour }}>Max Velocity (km/h)</td>
                                    <td>    
                                        {maxVelocity || "N/A"} km/h
                                    </td>
                                </tr>
                                <tr>
                                    <td style={{ color: driver.teamColour }}>Current Team</td>
                                    <td>{driver.teamName}</td>
                                </tr>
                                <tr>
                                    <td style={{ color: driver.teamColour }}>HeartRate</td>
                                    <td>{heartRate}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div className="details-right">
                        <div className="stat-box">
                            <h3>LEADERBOARD</h3>
                            <p style={{ color: driver.teamColour }}>#{driver.position}</p>
                        </div>
                        <div className="stat-box">
                            <h3>POINTS</h3>
                            <p style={{ color: driver.teamColour }}>0</p>
                        </div>
                    </div>
                </div>

                <div className="driver-chart-section">
                    <div className="driver-chart">
                        <h2 style={{ color: driver.teamColour }}>Lap Times</h2>
                        <Line data={lapChartData} options={chartOptions} />
                    </div>

                    <div className="lap-stats">
                        <h3 style={{ color: driver.teamColour }}>Lap Statistics</h3>
                        <p>
                            <strong>Fastest Lap:</strong> {fastestLapTime}s
                        </p>
                        <p>
                            <strong>Average Lap:</strong> {averageLapTime}s
                        </p>
                    </div>
                </div>

                <div className="driver-chart-section">
                    <div className="driver-chart">
                        <h2 style={{ color: driver.teamColour }}>Heart Rate Over Time</h2>
                        <Line data={heartRateChartData} options={heartRateChartOptions} />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DriverDetails;