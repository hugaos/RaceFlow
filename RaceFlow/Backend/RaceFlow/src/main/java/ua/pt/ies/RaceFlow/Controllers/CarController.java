package ua.pt.ies.RaceFlow.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Services.CarService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cars")
@Tag(name = "Car Controller", description = "Operations related to car management")
public class CarController {

    private final CarService carService;

    // Constructor-based dependency injection
    public CarController(CarService carService) {
        this.carService = carService;
    }

    // POST - Create a new car
    @PostMapping
    @Operation(summary = "Create a new car", description = "Adds a new car to the system with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Car created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Car> createCar(@RequestBody Map<String, Object> carData) {
        // Passa os dados diretamente para o serviço
        Car newCar = carService.createCar(carData);
        return new ResponseEntity<>(newCar, HttpStatus.CREATED);
    }
    
    // POST createAll
    @PostMapping("/createAll")
    public ResponseEntity<?> createCarsForAllDrivers() {
        carService.createCarsForAllDrivers(); // Lógica no serviço
        return ResponseEntity.ok("Cars created successfully for all drivers.");
    }
    // GET - Retrieve all cars
    @GetMapping
    @Operation(summary = "Get all cars", description = "Fetches a list of all cars in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cars retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<List<Car>> getAllCars() {
        List<Car> cars = carService.getAllCars();
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }

    // GET by driver ID - Retrieve a car by the driver's ID
    @GetMapping("/{driverId}")
    @Operation(summary = "Get car by driver ID", description = "Fetches a car by the driver's ID")
    @Parameter(name = "driverId", description = "ID of the driver associated with the car", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<Car> getCarByDriverId(@PathVariable Integer driverId) {
        Car car = carService.getCarByDriverId(driverId);
        return ResponseEntity.ok(car);
    }

    // DELETE by id - Delete a car by its ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car by ID", description = "Removes a car from the system by its unique ID")
    @Parameter(name = "id", description = "ID of the car to delete", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Car deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<Void> deleteCarById(@PathVariable Integer id) {
        carService.deleteCarById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT by id - Update a car by its ID
    @PutMapping("/{id}")
    @Operation(summary = "Update car by ID", description = "Updates the details of a car by its unique ID")
    @Parameter(name = "id", description = "ID of the car to update", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car updated successfully"),
            @ApiResponse(responseCode = "404", description = "Car not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Car> updateCar(@PathVariable Integer id, @RequestBody Car carDetails) {
        Car updatedCar = carService.updateCar(id, carDetails);
        return ResponseEntity.ok(updatedCar);
    }

    // GET - Retrieve car location by ID
    @GetMapping("/{id}/location")
    @Operation(summary = "Get car location by ID", description = "Fetches the location of a car by its unique ID")
    @Parameter(name = "id", description = "ID of the car to retrieve location", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car location retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<List<Float>> findLocationById(@PathVariable Integer id) {
        List<Float> location = carService.findLocationById(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping(params = "driverId")
    public ResponseEntity<Car> getCarByDriverNumber(@RequestParam Integer driverId) {
        Car car = carService.getCarByDriverId(driverId);
        return ResponseEntity.ok(car);
    }
}
