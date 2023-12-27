package roman.lazarchik.ApplicationManager.services;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.repositories.ApplicationHistoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ApplicationHistoryServiceTest {

    @Autowired
    private ApplicationHistoryService service;

    @MockBean
    private ApplicationHistoryRepository applicationRepository;

    @Test
    void whenSaveHistoryThenHistoryIsSaved() {

        ApplicationHistory history = new ApplicationHistory();
        history.setStatus(ApplicationStatus.CREATED);

        service.saveHistory(history);

        verify(applicationRepository, times(1)).save(history);
    }
}