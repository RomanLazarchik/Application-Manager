package roman.lazarchik.ApplicationManager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.repositories.ApplicationHistoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ApplicationHistoryServiceTest {

    @MockBean
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Test
    void whenSaveHistory_thenHistoryIsSaved() {
        ApplicationHistoryService applicationHistoryService = new ApplicationHistoryService(applicationHistoryRepository);

        ApplicationHistory history = new ApplicationHistory();

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationHistoryService.saveHistory(history);

        verify(applicationHistoryRepository).save(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertNotNull(capturedHistory);
    }
}