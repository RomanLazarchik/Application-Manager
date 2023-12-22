package roman.lazarchik.ApplicationManager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectDTO {
    @NotBlank(message = "Reason must not be empty")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}

