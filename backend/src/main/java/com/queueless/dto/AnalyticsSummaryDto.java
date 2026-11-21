package com.queueless.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsSummaryDto {
    private long totalTokensGenerated;
    private long currentlyWaiting;
    private double avgWaitTimeMinutes;
    private double avgServiceTimeMinutes;
    private long completedTokens;
    private long cancelledTokens;
    private long noShowTokens;
    private double noShowPercentage;
    private List<HourlyDistribution> hourlyDistribution;
    private List<ServicePerformance> serviceBreakdown;
    private List<CounterPerformance> counterBreakdown;

    public static class HourlyDistribution {
        private String hour;
        private long tokens;
        public HourlyDistribution() {}
        public HourlyDistribution(String hour, long tokens) {
            this.hour = hour;
            this.tokens = tokens;
        }
        public String getHour() { return hour; }
        public void setHour(String hour) { this.hour = hour; }
        public long getTokens() { return tokens; }
        public void setTokens(long tokens) { this.tokens = tokens; }
    }

    public static class ServicePerformance {
        private String serviceName;
        private long total;
        private long completed;
        private double avgWaitMin;
        private double avgServiceMin;

        public ServicePerformance() {}
        public ServicePerformance(String serviceName, long total, long completed, double avgWaitMin, double avgServiceMin) {
            this.serviceName = serviceName;
            this.total = total;
            this.completed = completed;
            this.avgWaitMin = avgWaitMin;
            this.avgServiceMin = avgServiceMin;
        }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public double getAvgWaitMin() { return avgWaitMin; }
        public void setAvgWaitMin(double avgWaitMin) { this.avgWaitMin = avgWaitMin; }
        public double getAvgServiceMin() { return avgServiceMin; }
        public void setAvgServiceMin(double avgServiceMin) { this.avgServiceMin = avgServiceMin; }
    }

    public static class CounterPerformance {
        private String counterName;
        private String staffName;
        private long tokensServed;
        private double avgServiceMin;

        public CounterPerformance() {}
        public CounterPerformance(String counterName, String staffName, long tokensServed, double avgServiceMin) {
            this.counterName = counterName;
            this.staffName = staffName;
            this.tokensServed = tokensServed;
            this.avgServiceMin = avgServiceMin;
        }

        public String getCounterName() { return counterName; }
        public void setCounterName(String counterName) { this.counterName = counterName; }
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
        public long getTokensServed() { return tokensServed; }
        public void setTokensServed(long tokensServed) { this.tokensServed = tokensServed; }
        public double getAvgServiceMin() { return avgServiceMin; }
        public void setAvgServiceMin(double avgServiceMin) { this.avgServiceMin = avgServiceMin; }
    }

    public AnalyticsSummaryDto() {}

    public long getTotalTokensGenerated() { return totalTokensGenerated; }
    public void setTotalTokensGenerated(long totalTokensGenerated) { this.totalTokensGenerated = totalTokensGenerated; }

    public long getCurrentlyWaiting() { return currentlyWaiting; }
    public void setCurrentlyWaiting(long currentlyWaiting) { this.currentlyWaiting = currentlyWaiting; }

    public double getAvgWaitTimeMinutes() { return avgWaitTimeMinutes; }
    public void setAvgWaitTimeMinutes(double avgWaitTimeMinutes) { this.avgWaitTimeMinutes = avgWaitTimeMinutes; }

    public double getAvgServiceTimeMinutes() { return avgServiceTimeMinutes; }
    public void setAvgServiceTimeMinutes(double avgServiceTimeMinutes) { this.avgServiceTimeMinutes = avgServiceTimeMinutes; }

    public long getCompletedTokens() { return completedTokens; }
    public void setCompletedTokens(long completedTokens) { this.completedTokens = completedTokens; }

    public long getCancelledTokens() { return cancelledTokens; }
    public void setCancelledTokens(long cancelledTokens) { this.cancelledTokens = cancelledTokens; }

    public long getNoShowTokens() { return noShowTokens; }
    public void setNoShowTokens(long noShowTokens) { this.noShowTokens = noShowTokens; }

    public double getNoShowPercentage() { return noShowPercentage; }
    public void setNoShowPercentage(double noShowPercentage) { this.noShowPercentage = noShowPercentage; }

    public List<HourlyDistribution> getHourlyDistribution() { return hourlyDistribution; }
    public void setHourlyDistribution(List<HourlyDistribution> hourlyDistribution) { this.hourlyDistribution = hourlyDistribution; }

    public List<ServicePerformance> getServiceBreakdown() { return serviceBreakdown; }
    public void setServiceBreakdown(List<ServicePerformance> serviceBreakdown) { this.serviceBreakdown = serviceBreakdown; }

    public List<CounterPerformance> getCounterBreakdown() { return counterBreakdown; }
    public void setCounterBreakdown(List<CounterPerformance> counterBreakdown) { this.counterBreakdown = counterBreakdown; }
}
