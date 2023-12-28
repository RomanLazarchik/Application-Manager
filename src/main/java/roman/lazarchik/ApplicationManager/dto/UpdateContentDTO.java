package roman.lazarchik.ApplicationManager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContentDTO {
    @NotBlank(message = "Content must not be empty")
    private String content;
}