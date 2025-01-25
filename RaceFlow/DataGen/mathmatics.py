import json
from math import sin, cos, sqrt, atan2, radians

# Open and read JSON file
def read_json_file(file_name):
    with open(file_name, 'r') as f:
        data = json.load(f)
    return data

# Write JSON file
def write_json_file(file_name, data):
    with open(file_name, 'w') as f:
        json.dump(data, f, indent=4)


# Haversine formula to calculate distance between two points
def haversine_distance(lat1, lon1, lat2, lon2):
    R = 6371e3  # Radius of Earth in meters
    phi1, phi2 = radians(lat1), radians(lat2)
    delta_phi = radians(lat2 - lat1)
    delta_lambda = radians(lon2 - lon1)
    a = sin(delta_phi / 2) ** 2 + cos(phi1) * cos(phi2) * sin(delta_lambda / 2) ** 2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c

# Divide a line segment into equidistant points
def divide_line(coord1, coord2, numpoints):
    lat1, lon1 = coord1
    lat2, lon2 = coord2
    lat = [lat1 + i * (lat2 - lat1) / numpoints for i in range(1, numpoints)]
    lon = [lon1 + i * (lon2 - lon1) / numpoints for i in range(1, numpoints)]
    return list(zip(lat, lon))


# Main function to process coordinates
def process_coordinates(file_name):
    coords = read_json_file(file_name)['coordinates']
    updated_coords = []

    for i in range(len(coords) - 1):
        lat1, lon1 = coords[i]
        lat2, lon2 = coords[i + 1]
        
        # Calculate distance between points
        distance = haversine_distance(lat1, lon1, lat2, lon2)
        
        # Determine number of points to add (5 meters apart)
        numpoints = max(1, int(distance // 5))
        
        # Add the original point
        updated_coords.append([lat1, lon1])
        
        # Add interpolated points
        interpolated_points = divide_line((lat1, lon1), (lat2, lon2), numpoints)
        updated_coords.extend(interpolated_points)

    # Add the last point
    updated_coords.append(coords[-1])

    # Write back to JSON
    write_json_file(file_name, {'coordinates': updated_coords})

# Run the process
process_coordinates('pitlane.json')
