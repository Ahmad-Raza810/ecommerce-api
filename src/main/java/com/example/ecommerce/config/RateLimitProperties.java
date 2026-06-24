package com.example.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private int defaultAnonymousCapacity = 20;
    private int defaultAnonymousRefill = 20;
    private int defaultAnonymousDurationMinutes = 1;

    private int defaultAuthenticatedCapacity = 100;
    private int defaultAuthenticatedRefill = 100;
    private int defaultAuthenticatedDurationMinutes = 1;

    private List<String> excludedPaths = new ArrayList<>();

    private Map<String, PathLimit> pathLimits = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultAnonymousCapacity() {
        return defaultAnonymousCapacity;
    }

    public void setDefaultAnonymousCapacity(int defaultAnonymousCapacity) {
        this.defaultAnonymousCapacity = defaultAnonymousCapacity;
    }

    public int getDefaultAnonymousRefill() {
        return defaultAnonymousRefill;
    }

    public void setDefaultAnonymousRefill(int defaultAnonymousRefill) {
        this.defaultAnonymousRefill = defaultAnonymousRefill;
    }

    public int getDefaultAnonymousDurationMinutes() {
        return defaultAnonymousDurationMinutes;
    }

    public void setDefaultAnonymousDurationMinutes(int defaultAnonymousDurationMinutes) {
        this.defaultAnonymousDurationMinutes = defaultAnonymousDurationMinutes;
    }

    public int getDefaultAuthenticatedCapacity() {
        return defaultAuthenticatedCapacity;
    }

    public void setDefaultAuthenticatedCapacity(int defaultAuthenticatedCapacity) {
        this.defaultAuthenticatedCapacity = defaultAuthenticatedCapacity;
    }

    public int getDefaultAuthenticatedRefill() {
        return defaultAuthenticatedRefill;
    }

    public void setDefaultAuthenticatedRefill(int defaultAuthenticatedRefill) {
        this.defaultAuthenticatedRefill = defaultAuthenticatedRefill;
    }

    public int getDefaultAuthenticatedDurationMinutes() {
        return defaultAuthenticatedDurationMinutes;
    }

    public void setDefaultAuthenticatedDurationMinutes(int defaultAuthenticatedDurationMinutes) {
        this.defaultAuthenticatedDurationMinutes = defaultAuthenticatedDurationMinutes;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    public Map<String, PathLimit> getPathLimits() {
        return pathLimits;
    }

    public void setPathLimits(Map<String, PathLimit> pathLimits) {
        this.pathLimits = pathLimits;
    }

    public static class PathLimit {
        private int capacity;
        private int refill;
        private int durationMinutes = 1;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefill() {
            return refill;
        }

        public void setRefill(int refill) {
            this.refill = refill;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
    }
}
