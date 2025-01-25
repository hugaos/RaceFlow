package ua.pt.ies.RaceFlow.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.pt.ies.RaceFlow.Entities.Driver;
import ua.pt.ies.RaceFlow.Services.DriverService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/drivers")
@Tag(name = "Driver Controller", description = "Operations related to driver management")
// @CrossOrigin(origins = "http://localhost")
public class DriverController {

    private final DriverService driverService;


    // POST - Create a new driver
    @PostMapping("/")
    @Operation(summary = "Create a new driver", description = "Adds a new driver to the system with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Driver> createDriver(@RequestBody Driver driver) {
        Driver newDriver = driverService.createDriver(driver);
        return ResponseEntity.ok(newDriver);
    }
    // POST - Criar todos os drivers da última sessão
    @PostMapping("/createAll")
    @Operation(summary = "Create all drivers", description = "Adds all drivers from the latest F1 session to the system with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drivers created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<Driver>> createAllDrivers() {
        List<Driver> drivers = driverService.createAllDrivers();
        return drivers.isEmpty() ? ResponseEntity.badRequest().build() : ResponseEntity.ok(drivers);
    }


    // GET - Retrieve all drivers
    @GetMapping
    @Operation(summary = "Get all drivers", description = "Fetches a list of all drivers in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Drivers retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<List<Driver>> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    // GET by id - Retrieve a driver by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID", description = "Fetches a driver by its unique ID")
    @Parameter(name = "id", description = "ID of the driver to retrieve", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<Driver> getDriverById(@PathVariable Integer id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(driver);
    }
    
    // DELETE by id - Delete a driver by ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete driver by ID", description = "Removes a driver from the system by its unique ID")
    @Parameter(name = "id", description = "ID of the driver to delete", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Driver deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<Void> deleteDriverById(@PathVariable Integer id) {
        driverService.deleteDriverById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT by id - Update a driver by ID
    @PutMapping("/{id}")
    @Operation(summary = "Update driver by ID", description = "Updates the details of a driver by their unique ID")
    @Parameter(name = "id", description = "ID of the driver to update", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver updated successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })

    public ResponseEntity<Driver> updateDriver(@PathVariable Integer id, @RequestBody Driver driverDetails) {
        Driver updatedDriver = driverService.updateDriver(id, driverDetails);
        return ResponseEntity.ok(updatedDriver);
    }

}