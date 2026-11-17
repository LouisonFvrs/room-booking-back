package fr.louison.gestionsallespringapi.recurrence.service;

import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.model.enums.RecurrenceType;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RecurrenceServiceTest {

    private final RecurrenceService recurrenceService = new RecurrenceService(mock(RecurrenceRepository.class));

    @Test
    void weeklyRecurrenceIncludesFirstStartAndStopsAtEndDate() {
        RecurrenceEntity recurrence = recurrence(RecurrenceType.WEEKLY, 1, LocalDate.of(2030, 1, 28));

        List<LocalDateTime> starts = recurrenceService.computeOccurrenceStarts(recurrence, LocalDateTime.of(2030, 1, 7, 10, 0));

        assertThat(starts).containsExactly(
                LocalDateTime.of(2030, 1, 7, 10, 0),
                LocalDateTime.of(2030, 1, 14, 10, 0),
                LocalDateTime.of(2030, 1, 21, 10, 0),
                LocalDateTime.of(2030, 1, 28, 10, 0)
        );
    }

    @Test
    void intervalSkipsPeriods() {
        RecurrenceEntity recurrence = recurrence(RecurrenceType.DAILY, 3, LocalDate.of(2030, 1, 10));

        List<LocalDateTime> starts = recurrenceService.computeOccurrenceStarts(recurrence, LocalDateTime.of(2030, 1, 1, 9, 0));

        assertThat(starts).extracting(LocalDateTime::getDayOfMonth).containsExactly(1, 4, 7, 10);
    }

    @Test
    void monthlyRecurrenceStartingOnThe31stDoesNotDriftAfterAShortMonth() {
        RecurrenceEntity recurrence = recurrence(RecurrenceType.MONTHLY, 1, LocalDate.of(2030, 4, 30));

        List<LocalDateTime> starts = recurrenceService.computeOccurrenceStarts(recurrence, LocalDateTime.of(2030, 1, 31, 14, 0));

        assertThat(starts).extracting(LocalDateTime::toLocalDate).containsExactly(
                LocalDate.of(2030, 1, 31),
                LocalDate.of(2030, 2, 28),
                LocalDate.of(2030, 3, 31),
                LocalDate.of(2030, 4, 30)
        );
    }

    @Test
    void recurrenceEndingBeforeTheFirstStartIsRejected() {
        RecurrenceEntity recurrence = recurrence(RecurrenceType.WEEKLY, 1, LocalDate.of(2030, 1, 1));

        assertThatThrownBy(() -> recurrenceService.computeOccurrenceStarts(recurrence, LocalDateTime.of(2030, 1, 7, 10, 0)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void seriesLongerThanTheMaximumIsRejected() {
        RecurrenceEntity recurrence = recurrence(RecurrenceType.DAILY, 1, LocalDate.of(2032, 1, 1));

        assertThatThrownBy(() -> recurrenceService.computeOccurrenceStarts(recurrence, LocalDateTime.of(2030, 1, 1, 10, 0)))
                .isInstanceOf(BadRequestException.class);
    }

    private RecurrenceEntity recurrence(RecurrenceType type, int intervalValue, LocalDate endDate) {
        return RecurrenceEntity.builder()
                .idRecurrence(1L)
                .type(type)
                .intervalValue(intervalValue)
                .endDate(endDate)
                .build();
    }
}
