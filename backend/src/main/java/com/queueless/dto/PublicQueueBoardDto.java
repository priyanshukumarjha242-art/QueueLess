package com.queueless.dto;

import java.util.List;

public class PublicQueueBoardDto {
    private Long organizationId;
    private String organizationName;
    private List<LiveQueueItemDto> nowCalling;
    private List<LiveQueueItemDto> waiting;
    private List<LiveQueueItemDto> recentlyCompleted;
    private long totalWaiting;
    private long totalServedToday;

    public PublicQueueBoardDto() {}

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public List<LiveQueueItemDto> getNowCalling() { return nowCalling; }
    public void setNowCalling(List<LiveQueueItemDto> nowCalling) { this.nowCalling = nowCalling; }

    public List<LiveQueueItemDto> getWaiting() { return waiting; }
    public void setWaiting(List<LiveQueueItemDto> waiting) { this.waiting = waiting; }

    public List<LiveQueueItemDto> getRecentlyCompleted() { return recentlyCompleted; }
    public void setRecentlyCompleted(List<LiveQueueItemDto> recentlyCompleted) { this.recentlyCompleted = recentlyCompleted; }

    public long getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(long totalWaiting) { this.totalWaiting = totalWaiting; }

    public long getTotalServedToday() { return totalServedToday; }
    public void setTotalServedToday(long totalServedToday) { this.totalServedToday = totalServedToday; }
}
