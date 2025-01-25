import random
import time
from kafka import KafkaProducer
from kafka.errors import KafkaError
from kafka import KafkaConsumer
import os
import json
from math import radians, sin, cos, sqrt, atan2, degrees
import requests

# Load coordinates
with open("cords.json", "r") as f:
    COORDINATES = json.load(f)

with open("pitlane.json", "r") as f:
    PITLANE = json.load(f)

COORDINATES = COORDINATES["coordinates"]
PITLANE = PITLANE["coordinates"]


# Create Kafka producer
def create_kafka_producer(broker):
    try:
        print(f"Connecting to Kafka broker at {broker}")
        return KafkaProducer(
            bootstrap_servers=broker,
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
    except KafkaError as e:
        print(f"Error creating Kafka producer: {e}")
        exit(1)

KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka:9092")
TOPIC_NAME_RACEFLOW = os.getenv("TOPIC_NAME", "drivers")
TOPIC_NAME_CARS = os.getenv("TOPIC_NAME_CARS", "cars")
TOPIC_NAME_RACE = os.getenv("TOPIC_NAME", "race")
TOPIC_NAME_PITSTOP = os.getenv("TOPIC_NAME", "pitstop")
TOPIC_NAME_LAPTIMES = os.getenv("TOPIC_NAME", "lap-times")

# Tyre selection based on weather
tyres = ["soft", "medium", "hard", "intermediate", "wet"]

def select_tyre(weather):
    if weather in ["sunny", "foggy"]:
        return random.choice(["soft", "medium", "hard"])
    elif weather == "rainy":
        return random.choice(["intermediate", "wet"])
    return random.choice(tyres)

# Initialize Kafka producer
producer = create_kafka_producer([KAFKA_BROKER])

# Haversine distance and other utility functions remain unchanged
def calculate_race_time(base_start_time):
    return time.time() - base_start_time
    


def calculate_time_gap(leader_progress, driver_progress, position_to_sector, velocities):
    """
    Calcula o gap de tempo entre o líder e um piloto específico.
    """
    if driver_progress == leader_progress:
        return 0.0

    gap_distance = 0.0
    num_positions = len(COORDINATES)
    leader_position = leader_progress % num_positions
    driver_position = driver_progress % num_positions

    current_position = driver_position
    while current_position != leader_position:
        next_position = (current_position + 1) % num_positions
        segment_distance = haversine_distance(
            COORDINATES[current_position][1], COORDINATES[current_position][0],
            COORDINATES[next_position][1], COORDINATES[next_position][0]
        )
        gap_distance += segment_distance
        current_position = next_position

    driver_velocity = velocities.get(driver_position, 200)  # Velocidade padrão
    time_gap = (gap_distance / driver_velocity) * 3600  # Converter horas para segundos
    return round(time_gap, 2)

def haversine_distance(lat1, lon1, lat2, lon2):
    R = 6371.0  # Earth radius in kilometers
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c

def calculate_angle(coord1, coord2, coord3):
    x1, y1 = coord1
    x2, y2 = coord2
    x3, y3 = coord3
    vector1 = (x2 - x1, y2 - y1)
    vector2 = (x3 - x2, y3 - y2)
    angle1 = atan2(vector1[1], vector1[0])
    angle2 = atan2(vector2[1], vector2[0])
    angle = degrees(abs(angle1 - angle2))
    if angle > 180:
        angle = 360 - angle
    return angle

# Classify segments based on angle and distance
def classify_segments(coordinates, curve_threshold=15, distance_threshold=0.05):
    segments = []
    for i in range(1, len(coordinates) - 1):
        angle = calculate_angle(coordinates[i - 1], coordinates[i], coordinates[i + 1])
        distance = haversine_distance(coordinates[i][1], coordinates[i][0], coordinates[i + 1][1], coordinates[i + 1][0])
        if angle > curve_threshold or distance < distance_threshold:
            segments.append(("curve", i))
        else:
            segments.append(("straight", i))
    return segments

def group_into_sectors(segments):
    sectors = []
    current_type = segments[0][0]
    start_index = segments[0][1]
    for i in range(1, len(segments)):
        segment_type, index = segments[i]
        if segment_type != current_type:
            sectors.append((current_type, start_index, index - 1))
            current_type = segment_type
            start_index = index
    sectors.append((current_type, start_index, len(segments) - 1))
    return sectors

def generate_velocity_for_sector(sector_type, elapsed_time):
    scaling_factor = min(1, 0.5 + (elapsed_time / 40))  # Slow start scaling factor, increases to 1 after 20 seconds

    if sector_type == "curve":
        base_velocity = random.uniform(150, 200)
    else:
        base_velocity = random.uniform(200, 350)

    return base_velocity * scaling_factor

def adjust_tyre_temperatures(base_temp=100.0):
    return [
        base_temp + random.uniform(-2.0, 2.0),  # Front left tyre
        base_temp + random.uniform(-1.5, 1.5),  # Front right tyre
        base_temp + random.uniform(-1.0, 1.0),  # Rear left tyre
        base_temp + random.uniform(-1.0, 1.0),  # Rear right tyre
    ]

def calculate_heart_rate(position, velocity, sector_type, lap):
    base_heart_rate = 80
    velocity_factor = 1 + (velocity / 200) * 0.5
    sector_factor = 1.2 if sector_type == "curve" else 1.0
    fatigue_factor = 1 + (lap / 70) * 0.15
    heart_rate = base_heart_rate  * velocity_factor * sector_factor * fatigue_factor
    heart_rate += random.uniform(-8, 8)
    heart_rate = max(80, min(200, heart_rate))
    return round(heart_rate)

def next_tyre(tyre, weather):
    if weather in ["sunny", "foggy"]:
        if tyre == "soft":
            return random.choice(["medium", "hard"])
        elif tyre == "medium":
            return random.choice(["soft", "hard"])
        elif tyre == "hard":
            return random.choice(["soft", "medium"])
    elif weather == "rainy":
        if tyre == "intermediate":
            return "wet"
        elif tyre == "wet":
            return "intermediate"
    return random.choice(tyres)

# Função para calcular a marcha (gear) com base na velocidade
def calculate_gear(speed):
    if speed <= 20:  # Ajustado para incluir uma faixa para a 1ª marcha
        return 1
    elif speed <= 80:
        return 2
    elif speed <= 120:
        return 3
    elif speed <= 160:
        return 4
    elif speed <= 200:
        return 5
    elif speed <= 250:
        return 6
    elif speed <= 300:
        return 7
    else:
        return 8

def calculate_rpm(speed, gear):
    gear_ratios = {
        1: 3.8, 
        2: 3.0,
        3: 2.4,
        4: 2.0,
        5: 1.6,
        6: 1.3,
        7: 1.1,
        8: 0.9,  
    }

    wheel_radius = 0.33  
    max_rpm = 15000      

    rpm = (speed * gear_ratios[gear]) / wheel_radius

    rpm = min(rpm, max_rpm)  
    rpm += random.uniform(-100, 100)  

    min_rpm = 800 * gear
    rpm = max(rpm, min_rpm)  
    return int(rpm)

def find_closest_coordinate_index(target, coordinates):
    min_distance = float('inf')
    closest_index = -1
    for i, coord in enumerate(coordinates):
        distance = haversine_distance(target[1], target[0], coord[1], coord[0])
        if distance < min_distance:
            min_distance = distance
            closest_index = i
    return closest_index

def race_running(url):
    response = requests.get(url)

    if response.status_code == 200:
        result = response.json()
        if result == True:
            return True
    return False

# Simulate the race with pitstop logic
def simulate_real_time_race(producer, topic_raceflow, topic_cars, topic_race, topic_pitstop, topic_laptimes):
    drivers = requests.get('http://raceflow-app:8080/api/drivers')
    driver_ids = [x['id'] for x in drivers.json()]
    print(driver_ids)

    # Embaralhar os IDs dos drivers
    random.shuffle(driver_ids)
    print(driver_ids)

    # Criar as posições iniciais dos carros com ordem embaralhada
    car_positions = {driver_id: 0 for driver_id in driver_ids}  # Start all cars at position 0
    car_laps = {driver_id: 1 for driver_id in driver_ids}
    line_start_position = 0  # Starting position in the coordinates
    last_position = {driver_id: None for driver_id in driver_ids}
    segments = classify_segments(COORDINATES)
    sectors = group_into_sectors(segments)

    # Lap times dictionary driver_id: last_lap_time
    last_lap_times = {driver_id: time.time() for driver_id in driver_ids}

    # Pitstop info dictionary

    pitstop_info = {
        driver_id: {
            "start_time": None,
            "duration": 0,
            "tyre": None,
            "in_pitstop": False,
            "pit_index": 0,
            "lap": None,
            "next_step_time": None,
            "pitstop_sent": False,
        }
        for driver_id in driver_ids
    }

    average_speeds = {driver_id: None for driver_id in driver_ids}
    velocity_buffers = {driver_id: [] for driver_id in driver_ids}
    last_average_calculation_time = time.time()

    max_speeds = {driver_id: 0 for driver_id in driver_ids}


    response = requests.get('http://raceflow-app:8080/api/races')
    races = response.json()

    race_weather = races[0]['weather']
    race_id=races[0]['id']
    race_laps=races[0]['totalLaps']

    url = f'http://raceflow-app:8080/api/races/{race_id}/running'

    driver_tyres = {driver_id: select_tyre(race_weather) for driver_id in driver_ids}

    PIT_ENTRY = PITLANE[0]
    PIT_EXIT = PITLANE[-1]

    # Map PITLANE coordinates to COORDINATES indices
    pitlane_indices = [find_closest_coordinate_index(coord, COORDINATES) for coord in PITLANE]


    # Introduce a random delay for each driver at the start of the race

    # Garantir tempos de início diferentes, com pelo menos 0.5 segundos entre cada carro
    base_start_time = time.time()
    start_times = {
        driver_id: base_start_time + i * 0.5 for i, driver_id in enumerate(driver_ids)

    }
    

    while True:
        if not race_running(url):
            time.sleep(1)
            continue

        elapsed_time = calculate_race_time(base_start_time)

        for driver_id in driver_ids:
            
            current_time = time.time()
            if current_time < start_times[driver_id]:
                continue  # Skip this driver until their start time
            
            current_position = car_positions[driver_id]
            current_tyre = driver_tyres[driver_id]

            # Pitstop handling
            if pitstop_info[driver_id]["in_pitstop"]:
                current_time = time.time()
                pit_index = pitstop_info[driver_id]["pit_index"]

                # Check if it's time to move to the next pit lane position
                if current_time >= pitstop_info[driver_id]["next_step_time"]:
                    if pit_index < len(pitlane_indices) - 1:
                        # Move to the next pit lane position
                        pitstop_info[driver_id]["pit_index"] += 1
                        current_position = pitlane_indices[pitstop_info[driver_id]["pit_index"]]

                        # Calculate the next step time
                        total_duration = pitstop_info[driver_id]["duration"]
                        time_per_step = total_duration / len(pitlane_indices)
                        pitstop_info[driver_id]["next_step_time"] = current_time + time_per_step

                        # Send pit stop data if not already sent
                        if not pitstop_info[driver_id]["pitstop_sent"]:
                            pitstop_data = {
                                "driverID": driver_id,
                                "start_time": pitstop_info[driver_id]["start_time"],
                                "duration": pitstop_info[driver_id]["duration"],
                                "new_tyre": next_tyre(driver_tyres[driver_id], race_weather),
                                "lap": pitstop_info[driver_id]["lap"],
                                "location": PITLANE[pit_index],
                            }
                            try:
                                producer.send(topic_pitstop, value=pitstop_data)
                                print(f"Sent pitstop data: {pitstop_data}")
                                pitstop_info[driver_id]["pitstop_sent"] = True
                            except Exception as e:
                                print(f"Error sending pitstop data: {e}")
                    else:
                        driver_tyres[driver_id] = pitstop_data["new_tyre"]
                        # Pitstop complete, rejoin the track at PIT_EXIT
                        pitstop_info[driver_id]["in_pitstop"] = False
                        pitstop_info[driver_id]["pit_index"] = 0
                        pitstop_info[driver_id]["pitstop_sent"] = False  # Reset for next pitstop

                        # Set current position to PIT_EXIT after the pit stop
                        current_position = pitlane_indices[-1]

                        # Ensure the car resumes from a valid point in the race
                        # Find the correct sector for the current position
                        for sector in sectors:
                            if sector[1] <= current_position <= sector[2]:
                                sector_type = sector[0]
                                break
                        else:
                            sector_type = "straight"  # Default to straight if no sector found

                        # Generate velocity after exiting the pit lane
                        velocity = generate_velocity_for_sector(sector_type, base_start_time + calculate_race_time(base_start_time))

                        # Update lap count
                        car_positions[driver_id] = current_position

                # Send car data during pitstop
                car_location = PITLANE[pitstop_info[driver_id]["pit_index"]]
                cars_data = {
                    "driverID": driver_id,
                    "location": car_location,
                    "next_location": next_location,
                    "tyreTemp": tyre_temperatures,
                    "currentLap": car_laps[driver_id],
                    "tyreType": driver_tyres[driver_id],
                    "currentSpeed": velocity,
                    "gear": gear,
                    "rpm": rpm,
                }
                try:
                    producer.send(topic_cars, value=cars_data)
                except Exception as e:
                    print(f"Error sending car data during pitstop: {e}")

                # Send driver data during pitstop
                driver_data = {
                    "driverID": driver_id,
                    "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
                    "currentLap": car_laps[driver_id],
                    "velocity": 60,  # Fixed velocity during pitstop
                    "heartRate": calculate_heart_rate(current_position, 60, "pitstop", car_laps[driver_id]),
                }
                try:
                    producer.send(topic_raceflow, value=driver_data)
                except Exception as e:
                    print(f"Error sending driver data during pitstop: {e}")

                # Continue to next iteration if in pit stop
                continue

            # Regular track progression (not in pitstop)
            sector_type = "straight"
            for sector in sectors:
                if sector[1] <= current_position <= sector[2]:
                    sector_type = sector[0]
                    break

            # Update position and velocity
            velocity = generate_velocity_for_sector(sector_type, elapsed_time)
            velocity_buffers[driver_id].append(velocity)

            current_time = time.time()
            if current_time - last_average_calculation_time >= 3:
                for d_id in driver_ids:
                    if velocity_buffers[d_id]:
                        # Calcular a média e armazenar no dicionário
                        average_speeds[d_id] = round(sum(velocity_buffers[d_id]) / len(velocity_buffers[d_id]), 2)
                # Resetar buffers e timer
                velocity_buffers = {driver_id: [] for driver_id in driver_ids}
                last_average_calculation_time = current_time

            gear = calculate_gear(velocity)
            rpm = calculate_rpm(velocity, gear)

            # Determine the number of positions to advance based on the velocity
            positions_to_advance = 1
            if velocity > 180:
                positions_to_advance = 5
            elif velocity > 150:
                positions_to_advance = 3
            elif velocity > 100:
                positions_to_advance = 2

            # Calculate the next position, respecting the limits of COORDINATES
            next_position = (current_position + positions_to_advance) % len(COORDINATES)
            current_position = next_position

            # Check if entering pit lane
            if (COORDINATES[current_position] == PIT_ENTRY and not pitstop_info[driver_id]["in_pitstop"] and random.random() < 0.6):
                pitstop_info[driver_id]["in_pitstop"] = True
                velocity = 60  # Fixed velocity during pitstop

                pitstop_info[driver_id]["start_time"] = time.time()
                pitstop_info[driver_id]["duration"] = random.uniform(20, 30)  # Pitstop duration (seconds)
                pitstop_info[driver_id]["lap"] = car_laps[driver_id]
                pitstop_info[driver_id]["pit_index"] = 0  # Start pit lane journey
                pitstop_info[driver_id]["next_step_time"] = time.time()  # Initialize next step timer
                current_position = pitlane_indices[0]  # Move to pit entry
                continue

            # Adjust tyre temperatures and calculate heart rate
            tyre_temperatures = adjust_tyre_temperatures()
            heart_rate = calculate_heart_rate(current_position, velocity, sector_type, car_laps[driver_id])

            # Lap increment when crossing start line
            if last_position[driver_id] is not None and last_position[driver_id] != current_position:
                if last_position[driver_id] < line_start_position <= current_position or (last_position[driver_id] > current_position and line_start_position <= current_position):
                    car_laps[driver_id] += 1
                    lap_time = current_time - last_lap_times[driver_id]
                    last_lap_times[driver_id] = current_time
                    try:
                        producer.send(topic_laptimes, value={"driverID": driver_id, "lapTime": round(lap_time, 3)})
                        print("Driver ID: ", driver_id, " Lap Time: ", round(lap_time, 3))
                    except Exception as e:
                        print(f"Error sending lap time data: {e}")

            last_position[driver_id] = current_position
            car_positions[driver_id] = current_position

            # Determine car location based on whether the car is in the pitstop
            if pitstop_info[driver_id]["in_pitstop"]:
                car_location = PITLANE[pitstop_info[driver_id]["pit_index"]]
                next_location = PITLANE[min(pitstop_info[driver_id]["pit_index"] + 1, len(PITLANE) - 1)]
                velocity = 60  # Fixed velocity during pitstop
                print(f"Sending car data for driver {driver_id} during pitstop")
                heart_rate = calculate_heart_rate(current_position, velocity, "pitstop", car_laps[driver_id])
            else:
                car_location = COORDINATES[current_position]
                next_location = COORDINATES[(current_position + 1) % len(COORDINATES)]

            # Send car data regardless of the pitstop status
            cars_data = {
                "driverID": driver_id,
                "location": car_location,
                "next_location": next_location,
                "tyreTemp": tyre_temperatures,
                "currentLap": car_laps[driver_id],
                "tyreType": driver_tyres[driver_id],
                "currentSpeed": velocity,
                "gear": gear,
                "rpm": rpm,
            }

            position_to_sector = {i: "straight" for i in range(len(COORDINATES))}  # Inicializar com 'straight'
            for sector_type, start, end in sectors:
                for position in range(start, end + 1):
                    position_to_sector[position] = sector_type

            progress = {
                driver_id: car_laps[driver_id] * len(COORDINATES) + car_positions[driver_id]
                for driver_id in driver_ids
            }
            leader_id = max(progress, key=progress.get)  # Carro com maior progresso
            leader_progress = progress[leader_id]
            leader_position = car_positions[leader_id]

            elapsed_time = calculate_race_time(base_start_time)

            velocities = {
                driver_id: generate_velocity_for_sector(
                    position_to_sector.get(car_positions[driver_id]), elapsed_time
                )
                for driver_id in driver_ids
            }


            gaps = {
                driver_id: calculate_time_gap(leader_progress, progress[driver_id], position_to_sector, velocities)
                for driver_id in driver_ids
            }
            # Send driver data regardless of the pitstop status 
            if velocity > max_speeds[driver_id]:  # Verificar se é maior que o máximo armazenado
                max_speeds[driver_id] = round(velocity, 2)
            
            driver_data = {
                "driverID": driver_id,
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
                "currentLap": car_laps[driver_id],
                "velocity": velocity,
                "averageVelocity": average_speeds[driver_id],  # Adicionar velocidade média
                "maxVelocity": max_speeds[driver_id],          # Velocidade máxima
                "heartRate": heart_rate,
                "gap": gaps[driver_id],
            }

            race_data = {
                "raceID": race_id,
                "currentLap": car_laps[driver_id],
                "weather": race_weather,
            }

            try:
                producer.send(topic_raceflow, value=driver_data)
                producer.send(topic_cars, value=cars_data)
                producer.send(topic_race, value=race_data)
            except Exception as e:
                print(f"Error sending data: {e}")

        time.sleep(0.1)  # Simulate real-time updates
def wait_for_start_signal(broker, topic_start_race):
    consumer = KafkaConsumer(
        topic_start_race,
        bootstrap_servers=broker,
        value_deserializer=lambda v: json.loads(v.decode('utf-8')),
    )
    print(f"Listening for start signal on topic {topic_start_race}...")
    for message in consumer:
        print(f"Received message: {message}")
        if message.value.get("start_race") == True:
            print("Received start signal, starting the race simulation...")
            break

if __name__ == "__main__":
    topic_start_race = os.getenv("START_RACE", "start-race")
    wait_for_start_signal([KAFKA_BROKER], topic_start_race)
    simulate_real_time_race(producer, TOPIC_NAME_RACEFLOW, TOPIC_NAME_CARS, TOPIC_NAME_RACE, TOPIC_NAME_PITSTOP, TOPIC_NAME_LAPTIMES)
