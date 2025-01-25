import React, { use, useEffect, useRef, useState } from "react";
import * as d3 from 'd3-geo';
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import geojson from '../assets/pt-2008.json';
import '../css/index.css';
import pitstopIcon from "../assets/pitstop.svg";


const F1Track = () => {
  const canvasRef = useRef(null);
  const [fastestLap, setFastestLap] = useState("No Fastest Lap Yet");
  const [currentLap, setCurrentLap] = useState(0);
  const [totalLaps, setTotalLaps] = useState(0);
  const [cars, setCars] = useState([]);
  const [events, setEvents] = useState([]); // Fila de eventos
  const interval = useRef(null); 
  const interval2 = useRef(null); 
  const carsRef = useRef(cars);
  const [currentRaceId, setCurrentRaceId] = useState(null);
  const [fastestCar, setFastestCar] = useState(null);
  const [driverName, setDriverName] = useState(null);

  const [raceStatus, setRaceStatus] = useState(false);

  useEffect(() => {
    localStorage.removeItem("events");
    localStorage.removeItem("currentRaceId");
  }, [currentRaceId]);

  // Novos estados para weather e trackTemperature
  const [weather, setWeather] = useState(null);
  const [trackTemperature, setTrackTemperature] = useState(null);

  let cars_ = [];
  let transformers = [];
  let currentLap_ = 0;

  const getWeatherIcon = (weather) => {
    switch (weather) {
      case "sunny":
        return "☀️";
      case "rainy":
        return "🌧️";
      case "foggy":
        return "🌫️";
      case "windy":
        return "💨";
      default:
        return "❓";
    }
  };

  useEffect(() => {
    carsRef.current = cars;
  }, [cars]);

  const updateEvents = (newEvent) => {

    setEvents((prevEvents) => {
      // Check if the event with the same ID already exists to prevent duplicates
      if (prevEvents.some(event => event.id === newEvent.id)) {
        return prevEvents; // Don't add the event if it's already in the list
      }
  
      const storedEvents = localStorage.getItem("events");
      const parsedEvents = storedEvents && storedEvents !== "null" ? JSON.parse(storedEvents) : [];
  
      const updatedEvents = [newEvent, ...parsedEvents];
  
      if (updatedEvents.length > 4) {
        updatedEvents.shift(); // Remove the oldest event if there are more than 4
      }
  
      // Remove the "isNew" state after 5 seconds to end the animation
      setTimeout(() => {
        setEvents((currentEvents) =>
          currentEvents.map((event) =>
            event.id === newEvent.id ? { ...event, isNew: false } : event
          )
        );
      }, 5000); // 5 seconds animation duration
  
      // Remove the event after 15 seconds
      setTimeout(() => {
        setEvents((currentEvents) => {
          const updatedEvents = currentEvents.filter((event) => event.id !== newEvent.id);
  
          // Update localStorage to reflect the list without the removed event
          localStorage.setItem("events", JSON.stringify(updatedEvents));
          
          return updatedEvents; // Return the updated events
        });
      }, 15000); // 15 seconds before removal
  
      // Save the updated events to localStorage
      localStorage.setItem("events", JSON.stringify(updatedEvents));
  
      return updatedEvents;
    });
  };
  

  useEffect(() => {
    const storedRaceId = localStorage.getItem("currentRaceId");
    if (storedRaceId) {
      setCurrentRaceId(parseInt(storedRaceId, 10));
      const storedEvents = localStorage.getItem(`events-race-${storedRaceId}`);
      if (storedEvents) {
        setEvents(JSON.parse(storedEvents));

      }
    }
  }, []);


  useEffect(() => {
    const storedEvents = localStorage.getItem("events");
    if (storedEvents) {
      setEvents(JSON.parse(storedEvents));

    }
  }, []);


  const updateCarData = (updatedCar) => {
    console.log("this is the updatedCar.carID" + updatedCar.carID);
    console.log(transformers);
    const updatedCars = transformers.map((car) => {
      console.log("this is the car.id" + car.id);
      if (car.id === updatedCar.carID) {
        return { ...car, ...updatedCar };
      }
      return car; // Certifique-se de retornar o carro original quando não corresponde
    });
    console.log(updatedCars);
    setCars(updatedCars);
  };

  const fetchFastestLap = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/races/1/fastest-lap");
      if (response.ok) {
        const data = await response.json();
        console.log("Fetched Fastest Lap:", data.fastestLap);
        if (fastestLap != data.fastestLap) {
          setFastestLap(data.fastestLap);
          setDriveridfastestLap(data.carId);
          for (let i = 0; i < cars.length; i++) {
            if (cars[i].id == data.carId) {
              setFastestCar(cars[i]);
              setDriverName(cars[i].driver.name);
            }
          }
        }
      }
    } catch (error) {
      console.error("Error fetching fastest lap:", error);
    }
  };


  // Race status
  useEffect(() => {
    const fetchRaceStatus = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/races/1/running", { method: "GET" });
        if (response.ok) {
          const fetchRaceStatus = await response.json();
  
          console.log('Race Status:', fetchRaceStatus);
  
          if (fetchRaceStatus === true) {
            setRaceStatus(true);
            // Remove "RedFlag" event if the race is resumed
            setEvents((prevEvents) => prevEvents.filter(event => event.title !== "Red Flag"));
          } else {
            setRaceStatus(false);
  
            // Check if the "Red Flag" event already exists
            const redFlagExists = events.some(event => event.title === "Red Flag");
            if (!redFlagExists) {
              const redFlagEvent = {
                id: Date.now(),
                iconClass: "event-icon red-flag",
                title: "Red Flag",
                details: "Race is stopped",
                backgroundColor: "#ff0000", // Red color for the RedFlag event
              };
              // Add event and ensure it is not removed after 15 seconds
              setEvents((prevEvents) => [redFlagEvent, ...prevEvents]);
            }
          }
        }
      } catch (error) {
        console.error("Error fetching race status:", error);
      }
    };
  
    fetchRaceStatus();
    
    // Update race status every 5 seconds
    interval.current = setInterval(() => {
      fetchRaceStatus();
    }, 5000);
  
    return () => {
      clearInterval(interval.current);
    };
  }, [events]); // Add events as a dependency to ensure we check the current events state
  
  
  

  useEffect(() => {
    // Fetch initial cars data
    const fetchCars = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/cars", { method: "GET" });
        if (response.ok) {
          const data = await response.json();
          console.log("Initial cars data:", data);
          transformers = data.map((car) => ({
            ...car,
            location: car.location.map((coord) => parseFloat(coord)) // Ensure location is an array of numbers
          }));
          setCars(data.map((car) => ({
            ...car,
            location: car.location.map((coord) => parseFloat(coord)) // Ensure location is an array of numbers
          })));
        }
      } catch (error) {
        console.error("Error fetching cars:", error);
      }
    };

    fetchCars();
    const interval = setInterval(() => {
      fetchCars();
    }, 500);

    const interval2 = setInterval(() => {
      fetchFastestLap();
    }, 10000);

    return () => {
      clearInterval(interval);
      clearInterval(interval2);
    };
  }, []);

  useEffect(() => {
    const socket = new SockJS("http://localhost:8080/api/websocket");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      console.log("Connected to WebSocket");

      stompClient.subscribe("/topic/cars", (message) => {
        const updatedCar = JSON.parse(message.body);
        console.log("Received WebSocket update:", updatedCar);
        // updateCarData(updatedCar); // Update car data with the incoming update if desired
      });

      // stompClient.subscribe("/topic/fastest-lap-update", (message) => {
      //   const data = JSON.parse(message.body);
      //   console.log("Fastest Lap Update:", data);
      //   setFastestLap({ fastestLap: data.fastestLap, carId: data.carId });
      // });

      stompClient.subscribe("/topic/race", (message) => {
        const raceData = JSON.parse(message.body);
        if (raceData.currentLap > currentLap_) {
          setCurrentLap(raceData.currentLap);
          currentLap_ = raceData.currentLap;
        }

        // Update weather and trackTemperature if present in raceData
        if (raceData.weather) {
          setWeather(raceData.weather);
        }
        if (raceData.trackTemperature !== undefined && raceData.trackTemperature !== null) {
          setTrackTemperature(raceData.trackTemperature);
        }
      });


      stompClient.subscribe("/topic/pitstop", (message) => {
        const pitstopData = JSON.parse(message.body);
        console.log("Received PITSTOP WebSocket update:", pitstopData); // Log the pitstop data

        // Check if the event already exists to avoid adding duplicates
        const driverId = pitstopData.driverId;
        const driver = carsRef.current.find((car) => car.id === driverId);
        const driverName = driver && driver.driver ? driver.driver.name : "Unknown";
        const driverNumber = driver && driver.driver ? driver.driver.driverNumber : "Unknown";
        const driverTeamColour = driver && driver.driver ? driver.driver.teamColour : "#000000";
        const formattedDuration = formatDuration(pitstopData.duration);

        const pitstopEvent = {
          id: Date.now(), // Using the current timestamp as the unique ID
          iconClass: "event-icon pitstop",
          title: "Pit Stop",
          details: `Driver ${driverName} (#${driverNumber || "?"}), Duration: ${formattedDuration}, Lap: ${pitstopData.lap || "?"}, Tyre: ${pitstopData.new_tyre || "?"}`,
          backgroundColor: `#${driverTeamColour}`
        };

        // Only add the event if it doesn't exist
        updateEvents(pitstopEvent);
 
      });


      // Helper function to format ISO 8601 duration (e.g., PT28S -> 28s)
      function formatDuration(duration) {
        if (!duration) return "Unknown";
        const match = duration.match(/PT(\d+)S/);
        return match ? `${match[1]}s` : duration;
      }
    }, (error) => {
      console.error("WebSocket connection error:", error);
    });

    return () => {
      if (stompClient.connected) {
        stompClient.disconnect(() => {
          console.log("WebSocket disconnected");
        });
      }
      clearInterval(interval);
    };
  }, []); // Ensure that the `cars` state is used here to get updated driver names


  useEffect(() => {
    const canvas = canvasRef.current;
    const context = canvas.getContext("2d");
    const PitLane = [[-80.239932, 25.96027],
    [-80.23879249999999, 25.95961975],
    [-80.237653, 25.958969500000002],
    [-80.2365135, 25.95831925],
    [-80.235374, 25.957669]];

    // Clear the canvas before drawing
    context.clearRect(0, 0, canvas.width, canvas.height);

    const padding = 20; // Padding around the canvas

    // Projection to transform GeoJSON coordinates into pixels
    const projection = d3.geoMercator()
      .fitExtent(
        [
          [padding, padding],
          [canvas.width - padding, canvas.height - padding]
        ],
        geojson
      );

    const pathGenerator = d3.geoPath(projection, context);

    // Draw the track
    geojson.features.forEach((feature) => {
      context.beginPath();
      pathGenerator(feature); // Draw the geometry

      // Outline the track with a light grey color
      context.lineWidth = 4; // Outline thickness
      context.strokeStyle = "#b0b0b0"; // Light grey outline color
      context.stroke(); // Draw the outline of the track path
    });

    // Draw the pit lane
    context.beginPath();
    const pitLanePath = PitLane.map((coord) => projection(coord));
    context.moveTo(pitLanePath[0][0], pitLanePath[0][1]);
    pitLanePath.slice(1).forEach((coord) => context.lineTo(coord[0], coord[1]));
    context.lineWidth = 4; // Outline thickness
    context.strokeStyle = "#b0b0b0"; // Black outline color
    context.stroke(); // Draw the outline of the pit lane path

    const startLine = [-80.237228, 25.9590515]

    // Make a point on the start line and turn it into a rectangle and rotate it 35 degrees to the right
    const startLinePoint = projection(startLine);
    context.save();
    context.translate(startLinePoint[0], startLinePoint[1]);
    context.rotate(35 * Math.PI / 180);
    context.fillStyle = "#000";
    context.fillRect(-3, -10, 6, 20);
    context.restore();


    // Draw cars on the track
    cars.forEach((car) => {
      const [lon, lat] = car.location;
      const projected = projection([lon, lat]);

      if (projected) {
        const [carX, carY] = projected;

        // Draw the circle representing the car
        context.beginPath();
        context.arc(carX, carY, 7, 0, 2 * Math.PI);
        context.fillStyle = car.teamColor ? "#" + car.teamColor : "#000";
        context.fill();

        // Draw the driver number inside the circle
        context.fillStyle = "#000";
        context.font = "10px Arial";
        context.textAlign = "center";
        context.textBaseline = "middle";
        context.fillText(car.driver.driverNumber, carX, carY);

        console.log(`Car ${car.id} drawn at (${carX}, ${carY})`);
      } else {
        console.error(`Projection failed for car ${car.id} at location:`, car.location);
      }
    });
  }, [cars]);

  const fetchTotalLaps = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/races", { method: "GET" });
      if (response.ok) {
        const races = await response.json();
        if (races.length > 0) {
          const firstRace = races[0]; // Pega a primeira corrida da lista
          console.log("Total Laps:", firstRace.totalLaps);
          setTotalLaps(firstRace.totalLaps); // Atualiza o estado com o total de voltas

          // Atualiza weather e trackTemperature se existirem
          if (firstRace.weather) {
            setWeather(firstRace.weather);
          }
          if (firstRace.trackTemperature !== undefined && firstRace.trackTemperature !== null) {
            setTrackTemperature(firstRace.trackTemperature);
          }
        } else {
          console.warn("No races found");
          setTotalLaps(0); // Reseta para 0 se não houver corridas
        }
      } else {
        console.error("Failed to fetch races:", response.statusText);
      }
    } catch (error) {
      console.error("Error fetching total laps:", error);
    }
  };

  fetchTotalLaps();

  return (
    <div className="quadrado">
      <div className="top-left-icons">
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "10px" }}>
          <span className="weather-icon" style={{ fontSize: '48px' }} title={weather ? weather : ""} >
            {weather ? getWeatherIcon(weather) : ""}

          </span>
          <span className="weather-temp">
            {trackTemperature !== null ? `${trackTemperature.toFixed(1)}°C` : ""}
          </span>
        </div>
        <p className="lap-text">
          Lap: <strong>{currentLap}</strong> / {totalLaps}
        </p>
        <div className="lap-progress-bar">
          <div
            className="lap-progress"
            style={{ width: `${(currentLap / totalLaps) * 100}%` }}
          ></div>
        </div>
      </div>

      <canvas ref={canvasRef} className="track-canvas" width={800} height={600}></canvas>

      {driverName && (
        <div className="fastest-lap">
          <div className="fastest-lap-icon">⏱</div>
          <div className="fastest-lap-info">
            <p>Driver {driverName}</p>
            <p className="fastest-lap-title">Fastest Lap</p>
            <p className="fastest-lap-time">{fastestLap} s</p>
          </div>
        </div>
      )}



      {events.length > 0 && (
      <div className="eventos">
        <div className="event-header">
          <span>EVENTS</span>
        </div>
        {events.map((evento) => (
          <div key={evento.id} className="event-item">
            <div
              className={`event-icon ${evento.iconClass}`}
              style={{
                backgroundImage: evento.iconClass.includes("pitstop")
                  ? `url(${pitstopIcon})`
                  : "none",
                backgroundSize: "contain",
                backgroundRepeat: "no-repeat",
                backgroundPosition: "center",
                backgroundColor: evento.backgroundColor,
              }}
            ></div>
            <div className="event-info">
              <p className="event-title">{evento.title}</p>
              <p className="event-details">{evento.details}</p>
            </div>
          </div>
        ))}
      </div>
    )}
      
    </div>
  );
};


export default F1Track;