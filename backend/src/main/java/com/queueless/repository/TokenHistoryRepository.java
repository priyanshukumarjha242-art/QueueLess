package com.queueless.repository;

import com.queueless.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {
    List<TokenHistory> findByTokenIdOrderByTimestampDesc(Long tokenId);
}
