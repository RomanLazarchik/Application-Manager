package roman.lazarchik.ApplicationManager.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roman.lazarchik.ApplicationManager.dto.ApplicationDTO;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ApplicationMapperTest {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Test
    public void testToEntity() {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setId(1L);
        applicationDTO.setName("Name");
        applicationDTO.setContent("Content");
        applicationDTO.setStatus(ApplicationStatus.CREATED);
        applicationDTO.setPublishedNumber(10);

        Application application = applicationMapper.toEntity(applicationDTO);

        assertEquals(applicationDTO.getId(), application.getId());
        assertEquals(applicationDTO.getName(), application.getName());
        assertEquals(applicationDTO.getContent(), application.getContent());
        assertEquals(applicationDTO.getStatus(), application.getStatus());
        assertEquals(applicationDTO.getPublishedNumber(), application.getPublishedNumber());
    }

    @Test
    public void testToDto() {
        Application application = new Application();
        application.setId(1L);
        application.setName("Name");
        application.setContent("Content");
        application.setStatus(ApplicationStatus.CREATED);
        application.setPublishedNumber(10);

        ApplicationDTO applicationDTO = applicationMapper.toDto(application);

        assertEquals(application.getId(), applicationDTO.getId());
        assertEquals(application.getName(), applicationDTO.getName());
        assertEquals(application.getContent(), applicationDTO.getContent());
        assertEquals(application.getStatus(), applicationDTO.getStatus());
        assertEquals(application.getPublishedNumber(), applicationDTO.getPublishedNumber());
    }
}