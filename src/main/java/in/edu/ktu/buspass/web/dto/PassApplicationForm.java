package in.edu.ktu.buspass.web.dto;

import in.edu.ktu.buspass.domain.PassType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class PassApplicationForm {

    @NotNull(message = "Choose a route")
    private Long routeId;

    @NotNull(message = "Choose a pass type")
    private PassType passType;

    @NotNull(message = "Choose a start date")
    @FutureOrPresent(message = "The start date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validFrom = LocalDate.now();

    @AssertTrue(message = "Please confirm that the details are correct")
    private boolean declaration;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public PassType getPassType() {
        return passType;
    }

    public void setPassType(PassType passType) {
        this.passType = passType;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public boolean isDeclaration() {
        return declaration;
    }

    public void setDeclaration(boolean declaration) {
        this.declaration = declaration;
    }
}
