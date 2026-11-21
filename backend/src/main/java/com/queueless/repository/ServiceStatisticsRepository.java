package com.queueless.repository;

import com.queueless.entity.ServiceStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceStatisticsRepository extends JpaRepository<ServiceStatistics, Long> {
    Optional<ServiceStatistics> findByServiceIdAndStatDate(Long serviceId, LocalDate statDate);
    List<ServiceStatistics> findByServiceIdAndStatDateBetween(Long serviceId, LocalDate start, LocalDate end);
    List<ServiceStatistics> findByServiceDepartmentOrganizationIdAndStatDateBetween(Long organizationId, LocalDate start, LocalDate end);
}
