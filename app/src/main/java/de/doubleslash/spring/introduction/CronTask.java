package de.doubleslash.spring.introduction;

import de.doubleslash.spring.introduction.repository.CarRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@AllArgsConstructor
@Component
@Configuration
@EnableScheduling
@Slf4j
public class CronTask {

    private final CarRepository repository;

    @Scheduled(cron = "${configuration.cron.schedule}")
    public void scheduledDeletionOfOldData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusHours(24);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        log.info("Beginning cron job: attempting to delete all data in table \"car\" older than %s".formatted(
                oneDayAgo.format(formatter)
        ));

        Instant oneDayAgoInstant = oneDayAgo.toInstant(ZoneOffset.of(ZoneId.systemDefault().getId()));
        repository.deleteAllByDateBefore(oneDayAgoInstant);

        log.info("Cron job successful");
    }

}