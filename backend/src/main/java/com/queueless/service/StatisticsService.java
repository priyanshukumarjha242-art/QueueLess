package com.queueless.service;

import com.queueless.dto.AnalyticsSummaryDto;
import com.queueless.entity.QueueToken;
import com.queueless.entity.TokenStatus;
import com.queueless.repository.QueueTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final QueueTokenRepository queueTokenRepository;

    public StatisticsService(QueueTokenRepository queueTokenRepository) {
        this.queueTokenRepository = queueTokenRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getAnalytics(Long organizationId, int days) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);

        List<QueueToken> tokens = queueTokenRepository.findByOrganizationIdAndDateRange(organizationId, startDate, endDate);

        AnalyticsSummaryDto summary = new AnalyticsSummaryDto();
        summary.setTotalTokensGenerated(tokens.size());

        long waiting = tokens.stream().filter(t -> t.getStatus() == TokenStatus.WAITING).count();
        long completed = tokens.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count();
        long cancelled = tokens.stream().filter(t -> t.getStatus() == TokenStatus.CANCELLED).count();
        long noShow = tokens.stream().filter(t -> t.getStatus() == TokenStatus.NO_SHOW).count();

        summary.setCurrentlyWaiting(waiting);
        summary.setCompletedTokens(completed);
        summary.setCancelledTokens(cancelled);
        summary.setNoShowTokens(noShow);

        double noShowRate = tokens.isEmpty() ? 0.0 : ((double) noShow / (double) tokens.size()) * 100.0;
        summary.setNoShowPercentage(Math.round(noShowRate * 10.0) / 10.0);

        // Calculate Average Wait Time (createdAt to calledAt)
        var waitDurations = tokens.stream()
                .filter(t -> t.getCalledAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> Math.max(0, Duration.between(t.getCreatedAt(), t.getCalledAt()).getSeconds()))
                .boxed()
                .collect(Collectors.toList());

        double avgWaitSec = waitDurations.isEmpty() ? 0 : waitDurations.stream().mapToLong(Long::longValue).average().orElse(0);
        summary.setAvgWaitTimeMinutes(Math.round((avgWaitSec / 60.0) * 10.0) / 10.0);

        // Calculate Average Service Time (servedAt/calledAt to completedAt)
        var serviceDurations = tokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getCompletedAt() != null)
                .mapToLong(t -> {
                    Instant start = t.getServedAt() != null ? t.getServedAt() : t.getCalledAt();
                    return start != null ? Math.max(0, Duration.between(start, t.getCompletedAt()).getSeconds()) : 0;
                })
                .filter(s -> s > 0)
                .boxed()
                .collect(Collectors.toList());

        double avgServiceSec = serviceDurations.isEmpty() ? 0 : serviceDurations.stream().mapToLong(Long::longValue).average().orElse(0);
        summary.setAvgServiceTimeMinutes(Math.round((avgServiceSec / 60.0) * 10.0) / 10.0);

        // Hourly distribution for peak hours
        Map<Integer, Long> hourlyCounts = new TreeMap<>();
        for (int h = 8; h <= 18; h++) {
            hourlyCounts.put(h, 0L);
        }

        ZoneId zone = ZoneId.systemDefault();
        for (QueueToken t : tokens) {
            ZonedDateTime zdt = t.getCreatedAt().atZone(zone);
            int hour = zdt.getHour();
            if (hour >= 8 && hour <= 18) {
                hourlyCounts.put(hour, hourlyCounts.getOrDefault(hour, 0L) + 1);
            }
        }

        List<AnalyticsSummaryDto.HourlyDistribution> hourlyList = hourlyCounts.entrySet().stream()
                .map(e -> new AnalyticsSummaryDto.HourlyDistribution(String.format("%02d:00", e.getKey()), e.getValue()))
                .collect(Collectors.toList());
        summary.setHourlyDistribution(hourlyList);

        // Service performance breakdown
        Map<String, List<QueueToken>> byService = tokens.stream()
                .collect(Collectors.groupingBy(t -> t.getService().getName()));

        List<AnalyticsSummaryDto.ServicePerformance> servicePerfList = new ArrayList<>();
        byService.forEach((serviceName, serviceTokens) -> {
            long total = serviceTokens.size();
            long comp = serviceTokens.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count();

            double sAvgWait = serviceTokens.stream()
                    .filter(t -> t.getCalledAt() != null && t.getCreatedAt() != null)
                    .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCalledAt()).toMinutes())
                    .average().orElse(0.0);

            double sAvgServ = serviceTokens.stream()
                    .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getCompletedAt() != null)
                    .mapToLong(t -> {
                        Instant start = t.getServedAt() != null ? t.getServedAt() : t.getCalledAt();
                        return start != null ? Duration.between(start, t.getCompletedAt()).toMinutes() : 0;
                    })
                    .average().orElse(0.0);

            servicePerfList.add(new AnalyticsSummaryDto.ServicePerformance(
                    serviceName, total, comp,
                    Math.round(sAvgWait * 10.0) / 10.0,
                    Math.round(sAvgServ * 10.0) / 10.0
            ));
        });
        summary.setServiceBreakdown(servicePerfList);

        // Counter performance breakdown
        Map<String, List<QueueToken>> byCounter = tokens.stream()
                .filter(t -> t.getServiceCounter() != null)
                .collect(Collectors.groupingBy(t -> t.getServiceCounter().getCounterNumber() + " - " + t.getServiceCounter().getName()));

        List<AnalyticsSummaryDto.CounterPerformance> counterPerfList = new ArrayList<>();
        byCounter.forEach((counterDesc, cTokens) -> {
            long cCompleted = cTokens.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count();
            String staff = cTokens.stream()
                    .filter(t -> t.getServiceCounter().getCurrentStaff() != null)
                    .map(t -> t.getServiceCounter().getCurrentStaff().getFullName())
                    .findFirst().orElse("Staff");

            double cAvgServ = cTokens.stream()
                    .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getCompletedAt() != null)
                    .mapToLong(t -> {
                        Instant start = t.getServedAt() != null ? t.getServedAt() : t.getCalledAt();
                        return start != null ? Duration.between(start, t.getCompletedAt()).toMinutes() : 0;
                    })
                    .average().orElse(0.0);

            counterPerfList.add(new AnalyticsSummaryDto.CounterPerformance(
                    counterDesc, staff, cCompleted, Math.round(cAvgServ * 10.0) / 10.0
            ));
        });
        summary.setCounterBreakdown(counterPerfList);

        return summary;
    }
}
