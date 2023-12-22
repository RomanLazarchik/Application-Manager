package roman.lazarchik.ApplicationManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ApplicationDTO {

        private Long id;
        private String name;
        private String content;
        private ApplicationStatus status;
        private Integer publishedNumber;
        private String reason;

    }
