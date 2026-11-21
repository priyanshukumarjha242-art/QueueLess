package com.queueless.repository;

import com.queueless.entity.QueueToken;
import com.queueless.entity.TokenStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    List<QueueToken> findByCustomerUserIdOrderByCreatedAtDesc(Long customerUserId);

    Optional<QueueToken> findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(Long customerUserId, List<TokenStatus> statuses);

    List<QueueToken> findByServiceIdAndStatusOrderByPriorityDescCreatedAtAsc(Long serviceId, TokenStatus status);

    List<QueueToken> findByDepartmentIdAndStatusOrderByPriorityDescCreatedAtAsc(Long departmentId, TokenStatus status);

    List<QueueToken> findByServiceCounterIdAndStatusInOrderByCreatedAtDesc(Long counterId, List<TokenStatus> statuses);

    List<QueueToken> findByServiceCounterIdOrderByCreatedAtDesc(Long counterId, Pageable pageable);

    List<QueueToken> findByOrganizationIdAndStatusIn(Long organizationId, List<TokenStatus> statuses);

    List<QueueToken> findByServiceIdAndStatusIn(Long serviceId, List<TokenStatus> statuses);

    @Query("SELECT COUNT(t) FROM QueueToken t WHERE t.service.id = :serviceId AND t.status = 'WAITING' AND (t.priority > :priority OR (t.priority = :priority AND t.createdAt < :createdAt))")
    long countPeopleAhead(@Param("serviceId") Long serviceId, @Param("priority") com.queueless.entity.TokenPriority priority, @Param("createdAt") Instant createdAt);

    @Query("SELECT COUNT(t) FROM QueueToken t WHERE t.service.id = :serviceId AND t.status = 'WAITING'")
    long countWaitingInService(@Param("serviceId") Long serviceId);

    @Query("SELECT COALESCE(MAX(t.sequenceNumber), 0) FROM QueueToken t WHERE t.service.id = :serviceId AND t.createdAt >= :startOfDay")
    int findMaxSequenceForToday(@Param("serviceId") Long serviceId, @Param("startOfDay") Instant startOfDay);

    @Modifying
    @Query("UPDATE QueueToken t SET t.status = :toStatus, t.serviceCounter.id = :counterId, t.calledAt = :now, t.recallCount = t.recallCount + 1 WHERE t.id = :tokenId AND (t.status = 'WAITING' OR t.status = 'CALLED')")
    int atomicCallToken(@Param("tokenId") Long tokenId, @Param("counterId") Long counterId, @Param("toStatus") TokenStatus toStatus, @Param("now") Instant now);

    @Query("SELECT t FROM QueueToken t WHERE t.organization.id = :organizationId AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<QueueToken> findByOrganizationIdAndDateRange(@Param("organizationId") Long organizationId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
