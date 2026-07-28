package fr.louison.gestionsallespringapi.recurrence.controller;

import fr.louison.gestionsallespringapi.recurrence.dto.RecurrenceDto;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.recurrence.service.RecurrenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurrence")
public class RecurrenceController {

    @Autowired
    private RecurrenceService recurrenceService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public List<RecurrenceDto.PostOutputDto> get() {
        return recurrenceService.getRecurrences();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<RecurrenceDto.PostOutputDto> create(
            @Valid @RequestBody RecurrenceDto.PostInputDto postInputDto
    ) {
        RecurrenceDto.PostOutputDto createdRecurrence = recurrenceService.createRecurrence(postInputDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecurrence);
    }
}
