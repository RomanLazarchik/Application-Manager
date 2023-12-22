package roman.lazarchik.ApplicationManager.mapper;

import org.springframework.stereotype.Component;
import roman.lazarchik.ApplicationManager.dto.ApplicationDTO;
import roman.lazarchik.ApplicationManager.models.Application;

@Component
public class ApplicationMapper {



    public Application toEntity(ApplicationDTO dto) {
        Application app = new Application();
        app.setId(dto.getId());
        app.setName(dto.getName());
        app.setContent(dto.getContent());
        app.setStatus(dto.getStatus());
        app.setPublishedNumber(dto.getPublishedNumber());
        app.setReason(dto.getReason());
        return app;
    }

    public ApplicationDTO toDto(Application entity) {
        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setId(entity.getId());
        appDTO.setName(entity.getName());
        appDTO.setContent(entity.getContent());
        appDTO.setStatus(entity.getStatus());
        appDTO.setPublishedNumber(entity.getPublishedNumber());
        appDTO.setReason(entity.getReason());
        return appDTO;
    }
}
