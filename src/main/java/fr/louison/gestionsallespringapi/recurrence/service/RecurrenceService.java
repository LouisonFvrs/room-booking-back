package fr.louison.gestionsallespringapi.recurrence.service;

import fr.louison.gestionsallespringapi.recurrence.dto.RecurrenceDto;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public RecurrenceEntity createRecurrence(RecurrenceDto.PostInputDto  postInputDto) {
        RecurrenceEntity recurrence = RecurrenceEntity.builder()
                .type(postInputDto.getType())
                .intervalValue(postInputDto.getIntervalValue())
                .endDate(postInputDto.getEndDate())
                .build();

        return recurrenceRepository.save(recurrence);
    }
}
