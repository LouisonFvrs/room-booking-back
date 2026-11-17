package fr.louison.gestionsallespringapi.recurrence.service;

import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.recurrence.dto.RecurrenceDto;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurrenceService {

    // A daily recurrence over one year; beyond that a series is almost certainly a mistake
    // and would create thousands of rows in a single request.
    public static final int MAX_OCCURRENCES = 366;

    private final RecurrenceRepository recurrenceRepository;

    public List<RecurrenceDto.PostOutputDto> getRecurrences() {
        return recurrenceRepository.findAll().stream()
                .map(this::mapToPostOutputDto)
                .toList();
    }

    public RecurrenceDto.PostOutputDto createRecurrence(RecurrenceDto.PostInputDto postInputDto) {
        if (postInputDto.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("La date de fin de récurrence doit être une date future.");
        }

        RecurrenceEntity recurrence = RecurrenceEntity.builder()
                .type(postInputDto.getType())
                .intervalValue(postInputDto.getIntervalValue())
                .endDate(postInputDto.getEndDate())
                .build();

        return mapToPostOutputDto(recurrenceRepository.save(recurrence));
    }

    /**
     * Start of every occurrence of a series, the first one included, up to the recurrence end date.
     */
    public List<LocalDateTime> computeOccurrenceStarts(RecurrenceEntity recurrence, LocalDateTime firstStart) {
        if (recurrence.getIntervalValue() < 1) {
            throw new BadRequestException("L'intervalle de récurrence doit être au moins égal à 1.");
        }
        if (recurrence.getEndDate().isBefore(firstStart.toLocalDate())) {
            throw new BadRequestException("La récurrence se termine avant le début de la réservation.");
        }

        List<LocalDateTime> starts = new ArrayList<>();
        for (int index = 0; ; index++) {
            LocalDateTime start = occurrenceStart(recurrence, firstStart, index);
            if (start.toLocalDate().isAfter(recurrence.getEndDate())) {
                return starts;
            }
            if (starts.size() == MAX_OCCURRENCES) {
                throw new BadRequestException(
                        "La récurrence génère plus de " + MAX_OCCURRENCES + " occurrences, réduisez la période ou augmentez l'intervalle."
                );
            }
            starts.add(start);
        }
    }

    // Each occurrence is computed from the first start rather than from the previous one:
    // a monthly series starting on the 31st would otherwise drift to the 28th for good after February.
    private LocalDateTime occurrenceStart(RecurrenceEntity recurrence, LocalDateTime firstStart, int index) {
        long step = (long) index * recurrence.getIntervalValue();

        return switch (recurrence.getType()) {
            case DAILY -> firstStart.plusDays(step);
            case WEEKLY -> firstStart.plusWeeks(step);
            case MONTHLY -> firstStart.plusMonths(step);
        };
    }

    private RecurrenceDto.PostOutputDto mapToPostOutputDto(RecurrenceEntity recurrence) {
        return RecurrenceDto.PostOutputDto.builder()
                .id(recurrence.getIdRecurrence())
                .type(recurrence.getType())
                .intervalValue(recurrence.getIntervalValue())
                .endDate(recurrence.getEndDate())
                .build();
    }
}
