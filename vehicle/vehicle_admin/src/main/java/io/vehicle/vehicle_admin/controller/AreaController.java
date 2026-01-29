package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.Area;
import io.vehicle.vehicle_admin.service.AreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/areas")
public class AreaController {
    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping
    public ResponseEntity<List<Area>> getAllAreas() {
        return ResponseEntity.ok(areaService.getAllAreas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Area> getAreaById(@PathVariable Integer id) {
        Area area = areaService.getAreaById(id);
        return area != null ?
                ResponseEntity.ok(area) :
                ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Area> createArea(@RequestBody Area area) {
        return ResponseEntity.ok(areaService.saveArea(area));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Integer id) {
        areaService.deleteArea(id);
        return ResponseEntity.noContent().build();
    }

}