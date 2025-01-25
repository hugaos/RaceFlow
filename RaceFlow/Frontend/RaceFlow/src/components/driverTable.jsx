import React, { useState, useEffect } from "react";
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import "../css/driverTable.css";
import { Link } from "react-router-dom";
import soft from "../assets/soft.svg";
import medium from "../assets/medium.svg";
import hard from "../assets/hard.svg";
import intermediate from "../assets/inter.svg";
import wet from "../assets/wet.svg";

function DriverTable() {
    const [drivers, setDrivers] = useState([]);
    const [cars, setCars] = useState({});
    const [loading, setLoading] = useState(true);

    const tyreImages = {
        soft: soft,
        medium: medium,
        hard: hard,
        intermediate: intermediate,
        wet: wet,
    };

    const updateDriverData = (updatedDriver) => {
        setDrivers((prevDrivers) => {
            const existingIndex = prevDrivers.findIndex(driver => driver.id === updatedDriver.id);
            if (existingIndex !== -1) {
                return prevDrivers.map((driver, index) =>
                    index === existingIndex ? { ...driver, ...updatedDriver } : driver
                );
            }
        });
    };

    const fetchCarDetails = async (driverId) => {
        try {
            const response = await fetch(`http://localhost:8080/api/cars/${driverId}`);
            if (response.ok) {
                const data = await response.json();
                setCars(prevCars => ({ ...prevCars, [driverId]: data }));
            } else {
                console.error("Error fetching car details:", response.statusText);
            }
        } catch (error) {
            console.error("Error fetching car details:", error);
        }
    };

    useEffect(() => {
        const fetchDrivers = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/drivers");
                if (response.ok) {
                    const data = await response.json();
                    setDrivers(data);
                    data.forEach(driver => fetchCarDetails(driver.id));
                } else {
                    console.error("API response error:", response.statusText);
                }
            } catch (error) {
                console.error("Error fetching drivers:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchDrivers();

        const socket = new SockJS("http://localhost:8080/api/websocket");
        const stompClient = Stomp.over(socket);

        stompClient.connect(
            {},
            () => {
                console.log("Connected to WebSocket");
                stompClient.subscribe("/topic/drivers", (message) => {
                    const data = JSON.parse(message.body);
                    const updatedDriver = data.driver;
                    updateDriverData(updatedDriver);
                    fetchCarDetails(updatedDriver.id);
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
    }, []);

    if (loading) {
        return <p>Loading...</p>;
    }

    // Sort drivers by gap (ascending order)
    const sortedDrivers = [...drivers].sort((a, b) => {
        const gapA = a.gap !== undefined && a.gap !== null ? parseFloat(a.gap) : Number.MAX_VALUE;
        const gapB = b.gap !== undefined && b.gap !== null ? parseFloat(b.gap) : Number.MAX_VALUE;

        // Se gapA for 0.0, coloca A em primeiro
        if (gapA === 0.0) {
            return -1;
        }
        if (gapB === 0.0) {
            return 1;
        }

        // Ordena pelo valor do gap/gap
        return gapA - gapB;
    });

    return (
        <table className="table table-striped">
            <thead className="tabel-head">
                <tr>
                    <th scope="col">Pos</th>
                    <th scope="col">Driver</th>
                    <th scope="col">Tyre</th>
                    <th scope="col">Battery</th>
                    <th scope="col">Gap</th>
                </tr>
            </thead>
            <tbody className="tab">
                {sortedDrivers.map((driver, index) => (
                    <tr key={driver.id} className={index % 2 === 0 ? "row-class-1" : "row-class-2"}>
                        <td>{index + 1}</td>
                        <td
                            className="driver-cell"
                            style={{
                                display: "flex",
                                alignItems: "center", // Alinha verticalmente ao centro
                                gap: "10px", // Espaçamento entre o círculo e o texto
                            }}
                        >
                            <span
                                style={{
                                    width: "25px", // Tamanho ajustado do círculo
                                    height: "26px", // Tamanho ajustado do círculo
                                    borderRadius: "50%",
                                    backgroundColor: `#${driver.teamColour || "444444"}`,
                                    display: "flex",
                                    justifyContent: "center",
                                    alignItems: "center",
                                    fontSize: "12px",
                                    color: "white",
                                    fontWeight: "bold",
                                }}
                            >
                                {driver.driverNumber}
                            </span>

                            <Link to={`/driver/${driver.id}`}>
                                {driver.acronym?.toUpperCase() || "N/A"}
                            </Link>
                        </td>


                        <td>
                            <img src={tyreImages[cars[driver.id]?.tyreType] || soft} alt="Tyre" />
                        </td>
                        <td className="driver-cell">
                            <Link to={`/car/${driver.id}`}>
                                {driver.battery || "100%"}
                            </Link>
                        </td>
                        <td>{driver.gap || "+0.000"} s</td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

export default DriverTable;
