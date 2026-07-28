package fr.louison.gestionsallespringapi.recurrence.service;

import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.recurrence.dto.RecurrenceDto;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurrenceService {

    @Autowired
    private RecurrenceRepository recurrenceRepository;

    public List<RecurrenceDto.PostOutputDto> getRecurrences(){
        List<RecurrenceEntity> recurrences = recurrenceRepository.findAll();
        return recurrences.stream()
                .map(recurrence -> RecurrenceDto.PostOutputDto.builder()
                        .id(recurrence.getIdRecurrence())
                        .type(recurrence.getType())
                        .intervalValue(recurrence.getIntervalValue())
                        .endDate(recurrence.getEndDate())
                        .build()
                )
                .toList();
    }

    public RecurrenceDto.PostOutputDto createRecurrence(RecurrenceDto.PostInputDto postInputDto) {

        if (postInputDto.getIntervalValue() != null && postInputDto.getIntervalValue() < 1) {
            throw new BadRequestException("L'intervalle de récurrence doit être au moins égal à 1.");
        }

        if (postInputDto.getEndDate() != null && postInputDto.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("La date de fin de récurrence doit être une date future.");
        }
        
        RecurrenceEntity recurrence = RecurrenceEntity.builder()
                .type(postInputDto.getType())
                .intervalValue(postInputDto.getIntervalValue())
                .endDate(postInputDto.getEndDate())
                .build();

        RecurrenceEntity savedRecurrence = recurrenceRepository.save(recurrence);

        return mapToPostOutputDto(savedRecurrence);
    }

    // Méthode helper de mapping privée réutilisable
    private RecurrenceDto.PostOutputDto mapToPostOutputDto(RecurrenceEntity recurrence) {
        return RecurrenceDto.PostOutputDto.builder()
                .id(recurrence.getIdRecurrence())
                .type(recurrence.getType())
                .intervalValue(recurrence.getIntervalValue())
                .endDate(recurrence.getEndDate())
                .build();
    }
}
