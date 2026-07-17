package fr.louison.gestionsallespringapi.recurrence.controller;

import fr.louison.gestionsallespringapi.recurrence.dto.RecurrenceDto;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.recurrence.service.RecurrenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurrence")
public class RecurrenceController {

    @Autowired
    private RecurrenceService recurrenceService;

    @GetMapping
    public List<RecurrenceDto.PostOutputDto> get() {
        return recurrenceService.getRecurrences();
    }

    @PostMapping("/create")
    public RecurrenceDto.PostOutputDto create(@Valid @RequestBody RecurrenceDto.PostInputDto  postInputDto) {
        RecurrenceEntity recurrence = recurrenceService.createRecurrence(postInputDto);

        return RecurrenceDto.PostOutputDto.builder()
                .id(recurrence.getIdRecurrence())
                .type(recurrence.getType())
                .intervalValue(recurrence.getIntervalValue())
                .endDate(recurrence.getEndDate())
                .build();
    }
}
