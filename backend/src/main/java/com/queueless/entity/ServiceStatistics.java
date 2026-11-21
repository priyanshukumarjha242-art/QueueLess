package com.queueless.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "service_statistics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"service_id", "stat_date"})
})
public class ServiceStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(nullable = false)
    private Integer totalGenerated = 0;

    @Column(nullable = false)
    private Integer totalServed = 0;

    @Column(nullable = false)
    private Integer totalCancelled = 0;

    @Column(nullable = false)
    private Integer totalNoShow = 0;

    @Column(nullable = false)
    private Long avgWaitSeconds = 0L;

    @Column(nullable = false)
    private Long avgServiceSeconds = 0L;

    public ServiceStatistics() {}

    public ServiceStatistics(ServiceEntity service, LocalDate statDate) {
        this.service = service;
        this.statDate = statDate;
        this.totalGenerated = 0;
        this.totalServed = 0;
        this.totalCancelled = 0;
        this.totalNoShow = 0;
        this.avgWaitSeconds = 0L;
        this.avgServiceSeconds = 0L;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }

    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }

    public Integer getTotalGenerated() { return totalGenerated; }
    public void setTotalGenerated(Integer totalGenerated) { this.totalGenerated = totalGenerated; }

    public Integer getTotalServed() { return totalServed; }
    public void setTotalServed(Integer totalServed) { this.totalServed = totalServed; }

    public Integer getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(Integer totalCancelled) { this.totalCancelled = totalCancelled; }

    public Integer getTotalNoShow() { return totalNoShow; }
    public void setTotalNoShow(Integer totalNoShow) { this.totalNoShow = totalNoShow; }

    public Long getAvgWaitSeconds() { return avgWaitSeconds; }
    public void setAvgWaitSeconds(Long avgWaitSeconds) { this.avgWaitSeconds = avgWaitSeconds; }

    public Long getAvgServiceSeconds() { return avgServiceSeconds; }
    public void setAvgServiceSeconds(Long avgServiceSeconds) { this.avgServiceSeconds = avgServiceSeconds; }
}
