package in.edu.ktu.buspass.domain;

public enum PassType {
    MONTHLY("Monthly", 1),
    SEMESTER("Semester", 6);

    private final String label;
    private final int months;

    PassType(String label, int months) {
        this.label = label;
        this.months = months;
    }

    public String getLabel() {
        return label;
    }

    public int getMonths() {
        return months;
    }
}
