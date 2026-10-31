package in.edu.ktu.buspass.domain;

public enum PassStatus {
    PENDING("Pending", "warning"),
    APPROVED("Approved", "success"),
    REJECTED("Rejected", "danger"),
    CANCELLED("Cancelled", "muted");

    private final String label;
    private final String badge;

    PassStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}
