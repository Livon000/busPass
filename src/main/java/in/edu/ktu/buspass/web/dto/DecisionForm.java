package in.edu.ktu.buspass.web.dto;

import jakarta.validation.constraints.Size;

public class DecisionForm {

    @Size(max = 255, message = "Remarks must be 255 characters or fewer")
    private String remarks;

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
