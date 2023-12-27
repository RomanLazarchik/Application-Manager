package roman.lazarchik.ApplicationManager.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import roman.lazarchik.ApplicationManager.dto.ApplicationDTO;
import roman.lazarchik.ApplicationManager.dto.DeleteDTO;
import roman.lazarchik.ApplicationManager.dto.RejectDTO;
import roman.lazarchik.ApplicationManager.exceptions.*;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.repositories.ApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private ApplicationHistoryService applicationHistoryService;

    @Test
    void whenCreateApplicationWithValidInputThenApplicationIsCreated() {

        Application app = new Application();
        app.setName("Name");
        app.setContent("Content");

        when(applicationRepository.save(any())).thenReturn(app);

        Application createdApp = applicationService.createApplication(app);

        assertEquals("Name", createdApp.getName());
        assertEquals("Content", createdApp.getContent());
        assertEquals(ApplicationStatus.CREATED, createdApp.getStatus());

        verify(applicationRepository, times(1)).save(any());
        verify(applicationHistoryService, times(1)).saveHistory(any());
    }

    @Test
    void whenCreateApplicationThenHistoryIsSaved() {

        Application app = new Application();
        app.setName("Name");
        app.setContent("Content");

        when(applicationRepository.save(any())).thenReturn(app);

        applicationService.createApplication(app);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);
        verify(applicationHistoryService).saveHistory(historyCaptor.capture());

        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(ApplicationStatus.CREATED, capturedHistory.getStatus());
        assertNotNull(capturedHistory.getTimestamp());

        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void whenCreateApplicationWithInvalidDataThenThrowInvalidInputException() {

        Application app = new Application();
        app.setName("");
        app.setContent(" ");

        assertThrows(InvalidInputException.class, () -> applicationService.createApplication(app));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenCreateApplicationWithMissingNameThenThrowInvalidInputException() {

        Application app = new Application();
        app.setContent("Content");

        assertThrows(InvalidInputException.class, () -> applicationService.createApplication(app));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenCreateApplicationWithMissingContentThenThrowInvalidInputException() {

        Application app = new Application();
        app.setName("Name");

        assertThrows(InvalidInputException.class, () -> applicationService.createApplication(app));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
     void whenCreateApplicationAlreadyExists() {

        Application app = new Application();
        app.setName("Name");
        app.setContent("Content");

        when(applicationRepository.findByNameAndContent("Name", "Content")).thenReturn(app);

        Application newApp = new Application();
        newApp.setName("Name");
        newApp.setContent("Content");

        Application result = applicationService.createApplication(newApp);

        verify(applicationRepository, times(1)).findByNameAndContent("Name", "Content");
        verify(applicationRepository, never()).save(any(Application.class));
        assertEquals(app, result);
    }

    @Test
    void whenUpdateContentSuccessfulUpdateAndReturnsUpdatedApplication() {

        long someId = 1L;
        String newContent = "New Content";

        Application app = new Application();
        app.setId(someId);
        app.setContent("Content");
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application updatedApplication = applicationService.updateContent(someId, newContent);

        assertThat(updatedApplication.getContent()).isEqualTo(newContent);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void whenUpdateContentThrowsExceptionApplicationNotFound() {

        long someId = 1L;
        String newContent = "New Content";

        when(applicationRepository.findById(someId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.updateContent(someId, newContent));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenUpdateContentThrowsExceptionContentEditNotAllowed() {

        long someId = 1L;
        String newContent = "New Content";

        Application app = new Application();
        app.setId(someId);
        app.setContent("Content");
        app.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(ContentEditNotAllowedException.class, () -> applicationService.updateContent(someId, newContent));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenUpdateContentWithNoChange() {

        long someId = 1L;
        String newContent = "Original Content";

        Application app = new Application();
        app.setId(someId);
        app.setContent("Original Content");
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application updatedApplication = applicationService.updateContent(someId, newContent);

        assertEquals(app.getId(), updatedApplication.getId());
        assertEquals("Original Content", updatedApplication.getContent());

        verify(applicationRepository, never()).save(any(Application.class));
    }
    @Test
    public void whenUpdateContentThenHistoryIsSave() {

        long someId = 1L;
        String newContent = "New Content";

        Application app = new Application();
        app.setId(someId);
        app.setContent("Content");
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        applicationService.updateContent(someId, newContent);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);
        verify(applicationHistoryService).saveHistory(historyCaptor.capture());

        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(someId, capturedHistory.getApplication().getId());
        assertTrue(capturedHistory.isContentUpdated());
        assertNotNull(capturedHistory.getTimestamp());
        assertEquals(ApplicationStatus.CREATED, capturedHistory.getStatus());

        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void whenRejectApplicationSuccessfulRejectionReturnsRejectedApplication() {

        long someId = 1L;
        RejectDTO reasonReject = new RejectDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application rejectedApplication = applicationService.rejectApplication(someId, reasonReject);

        assertThat(rejectedApplication.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(rejectedApplication.getReason()).isEqualTo(reasonReject.getReason());

        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void whenRejectApplicationThrowsExceptionApplicationNotFound() {

        long someId = 1L;
        RejectDTO reasonReject = new RejectDTO("Reason");

        when(applicationRepository.findById(someId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.rejectApplication(someId, reasonReject));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenRejectApplicationThrowsExceptionInvalidApplicationStatus() {

        long someId = 1L;
        RejectDTO reasonReject = new RejectDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(InvalidApplicationStatusException.class, () -> applicationService.rejectApplication(someId, reasonReject));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenRejectApplicationShouldThrowIllegalArgumentException() {

        long someId = 1L;
        RejectDTO reasonReject = new RejectDTO(" ");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(IllegalArgumentException.class, () -> applicationService.rejectApplication(someId, reasonReject),
                "A reason must be provided for rejecting an application. Please provide a valid reason in the 'reason' field.");

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenRejectApplicationThenHistoryIsSave() {

        long someId = 1L;
        RejectDTO rejectDTO = new RejectDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        ArgumentCaptor<ApplicationHistory> historyArgumentCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationService.rejectApplication(someId, rejectDTO);

        verify(applicationHistoryService).saveHistory(historyArgumentCaptor.capture());
        ApplicationHistory capturedHistory = historyArgumentCaptor.getValue();

        assertNotNull(capturedHistory.getTimestamp());
        assertEquals(ApplicationStatus.REJECTED, capturedHistory.getStatus());
        assertEquals(app, capturedHistory.getApplication());
        assertFalse(capturedHistory.isContentUpdated());
        assertEquals(someId, capturedHistory.getApplication().getId());

        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void whenDeleteApplicationSuccess() {

        long someId = 1L;
        DeleteDTO reasonDelete = new DeleteDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        applicationService.deleteApplication(someId, reasonDelete);

        assertEquals(ApplicationStatus.DELETED, app.getStatus());

        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void whenDeleteApplicationThrowsExceptionApplicationNotFound() {

        long someId = 1L;
        DeleteDTO reasonDelete = new DeleteDTO("Reason");

        when(applicationRepository.findById(someId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.deleteApplication(someId, reasonDelete));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenDeleteApplicationThrowsExceptionInvalidStatus() {

        long someId = 1L;
        DeleteDTO reasonDelete = new DeleteDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(InvalidApplicationStatusException.class, () -> applicationService.deleteApplication(someId, reasonDelete));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenDeleteApplicationThrowsInvalidApplicationStatusException() {

        long someId = 1L;
        DeleteDTO reasonDelete = new DeleteDTO("Reason");

        Application application = new Application();
        application.setId(someId);
        application.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(application));

        assertThrows(InvalidApplicationStatusException.class, () -> applicationService.deleteApplication(someId, reasonDelete));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenDeletingAlreadyDeletedApplicationThenNoChangesMade() {

        long someId = 1L;
        DeleteDTO reasonDelete = new DeleteDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.DELETED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        applicationService.deleteApplication(someId, reasonDelete);

        verify(applicationRepository, times(1)).findById(someId);

        assertEquals(ApplicationStatus.DELETED, app.getStatus());

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenDeleteApplicationThenHistoryIsSave() {

        long someId = 1L;
        DeleteDTO reasonDelete = new DeleteDTO("Reason");

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationService.deleteApplication(someId, reasonDelete);

        verify(applicationHistoryService).saveHistory(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(ApplicationStatus.DELETED, capturedHistory.getStatus());
        assertEquals(someId, capturedHistory.getApplication().getId());
        assertNotNull(capturedHistory.getTimestamp());
        assertEquals(app, capturedHistory.getApplication());

        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void whenVerifyApplicationSuccessfulVerificationReturnsVerifiedApplication() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application verifiedApplication = applicationService.verifyApplication(someId);

        assertThat(verifiedApplication.getStatus()).isEqualTo(ApplicationStatus.VERIFIED);
        assertThat(verifiedApplication.getContent()).isEqualTo(app.getContent());

        verify(applicationRepository, times(1)).save(any());
        verify(applicationHistoryService, times(1)).saveHistory(any());

    }

    @Test
    void whenVerifyApplicationThrowsExceptionApplicationNotFound() {

        long someId = 1L;

        when(applicationRepository.findById(someId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.verifyApplication(someId));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenVerifyApplicationThrowsExceptionInvalidApplicationStatus() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(InvalidApplicationStatusException.class, () -> applicationService.verifyApplication(someId));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenVerifyApplicationWithVerifiedStatus() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        Application result = applicationService.verifyApplication(someId);

        assertEquals(ApplicationStatus.VERIFIED, result.getStatus());
        assertEquals(app.getId(), result.getId());

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenVerifyApplicationThenHistoryIsSave() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationService.verifyApplication(someId);

        verify(applicationHistoryService).saveHistory(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(ApplicationStatus.VERIFIED, capturedHistory.getStatus());
        assertEquals(someId, capturedHistory.getApplication().getId());
        assertNotNull(capturedHistory.getTimestamp());
        assertEquals(app, capturedHistory.getApplication());

        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void whenAcceptApplicationSuccessfulAcceptanceReturnsAcceptedApplication() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application acceptedApplication = applicationService.acceptApplication(someId);

        assertThat(acceptedApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);

        verify(applicationRepository, times(1)).save(any());
        verify(applicationHistoryService, times(1)).saveHistory(any());
    }

    @Test
    void whenAcceptApplicationThrowsExceptionApplicationNotFound() {

        long someId = 1L;

        when(applicationRepository.findById(someId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.acceptApplication(someId));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenAcceptApplicationThrowsExceptionInvalidApplicationStatus() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(InvalidApplicationStatusException.class, () -> applicationService.acceptApplication(someId));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenAcceptApplicationWithAcceptedStatus() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        Application result = applicationService.acceptApplication(someId);

        assertEquals(ApplicationStatus.ACCEPTED, result.getStatus());
        assertEquals(app.getId(), result.getId());

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenAcceptApplicationThenHistoryIsSave() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationService.acceptApplication(someId);

        verify(applicationHistoryService).saveHistory(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(ApplicationStatus.ACCEPTED, capturedHistory.getStatus());
        assertEquals(someId, capturedHistory.getApplication().getId());
        assertNotNull(capturedHistory.getTimestamp());
        assertEquals(app, capturedHistory.getApplication());

        verify(applicationRepository, times(1)).save(any());
    }
    @Test
    void whenPublishApplicationSuccessfulAcceptanceReturnsPublishedApplication() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.findMaxPublishedNumber()).thenReturn(Optional.of(10));
        when(applicationRepository.save(any(Application.class))).then(returnsFirstArg());

        Application publishedApplication = applicationService.publishApplication(someId);

        assertNotNull(publishedApplication);
        assertEquals(ApplicationStatus.PUBLISHED, publishedApplication.getStatus());
        assertEquals(11, publishedApplication.getPublishedNumber().intValue());

        verify(applicationRepository, times(1)).save(any());
        verify(applicationHistoryService, times(1)).saveHistory(any());
    }

    @Test
    void whenPublishApplicationThrowsExceptionApplicationNotFound() {

        long someId = 1L;

        when(applicationRepository.findById(someId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.acceptApplication(someId));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void whenPublishApplicationThrowsExceptionInvalidApplicationStatus() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        assertThrows(InvalidApplicationStatusException.class, () -> applicationService.acceptApplication(someId));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenPublishApplicationWithPublishedStatus() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));

        Application result = applicationService.publishApplication(someId);

        assertEquals(ApplicationStatus.PUBLISHED, result.getStatus());
        assertEquals(app.getId(), result.getId());

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void whenPublishApplicationThenHistoryIsSave() {

        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(someId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationService.publishApplication(someId);

        verify(applicationHistoryService).saveHistory(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(ApplicationStatus.PUBLISHED, capturedHistory.getStatus());
        assertEquals(someId, capturedHistory.getApplication().getId());
        assertNotNull(capturedHistory.getTimestamp());
        assertEquals(app, capturedHistory.getApplication());
        assertFalse(capturedHistory.isContentUpdated());

        verify(applicationRepository, times(1)).save(any());
    }
    @Test
    public void whenGetApplicationsByNameAndStatusWithValidInput() {

        Application app1 = new Application(1L, "Name1", "Content1", ApplicationStatus.CREATED, null, null, 0);
        Application app2 = new Application(2L, "Name2", "Content2", ApplicationStatus.CREATED, null, null, 0);
        List<Application> applications = Arrays.asList(app1, app2);

        Page<Application> expectedPage = new PageImpl<>(applications);

        when(applicationRepository.findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class)))
                .thenReturn(expectedPage);

        Page<Application> resultPage = applicationService.getApplicationsByNameAndStatus("Name", ApplicationStatus.CREATED, 0, 10);

        assertEquals(2, resultPage.getTotalElements());
        verify(applicationRepository, times(1)).findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class));
    }
    @Test
    void whenGetApplicationsByNameAndStatusThrowsExceptionApplicationNotFound() {

        when(applicationRepository.findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class)))
                .thenThrow(new ApplicationNotFoundException("No applications found with the provided name and status"));

        assertThrows(ApplicationNotFoundException.class, () ->
                        applicationService.getApplicationsByNameAndStatus("Name", ApplicationStatus.CREATED, 0, 10),
                "Expected ApplicationNotFoundException to be thrown when no applications are found");

        verify(applicationRepository, times(1)).findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class));
    }

    @Test
    void whenGetApplicationsByNameAndStatusThrowsExceptionInvalidInput() {

        assertThrows(InvalidInputException.class, () ->
                        applicationService.getApplicationsByNameAndStatus(" ", ApplicationStatus.CREATED, 0, 10),
                "Name parameter must not be empty");

        verify(applicationRepository, never()).findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class));
    }
    @Test
    public void whenGetApplicationsByNameAndStatusThrowsExceptionDatabaseOperation() {

        when(applicationRepository.findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class)))
                .thenThrow(new DataAccessException("Test exception") {});

        assertThrows(DatabaseOperationException.class, () ->
            applicationService.getApplicationsByNameAndStatus("Name", ApplicationStatus.CREATED, 0, 10));

        verify(applicationRepository, times(1)).findByNameContainingAndStatus(anyString(), any(ApplicationStatus.class), any(PageRequest.class));
    }

    @Test
    public void whenGetApplicationsByNameWithValidInput() {

        Application app1 = new Application(1L, "Name1", "Content1", ApplicationStatus.CREATED, null, null, 0);
        Application app2 = new Application(2L, "Name2", "Content2", ApplicationStatus.CREATED, null, null, 0);
        List<Application> applications = Arrays.asList(app1, app2);

        Page<Application> expectedPage = new PageImpl<>(applications);

        when(applicationRepository.findByNameContaining(anyString(), any(PageRequest.class))).thenReturn(expectedPage);

        Page<Application> resultPage = applicationService.getApplicationsByName("Name", 0, 10);

        assertEquals(2, resultPage.getTotalElements());

        verify(applicationRepository, times(1)).findByNameContaining(anyString(), any(PageRequest.class));
    }
    @Test
    void whenGetApplicationsByNameThrowsExceptionApplicationNotFound() {

        when(applicationRepository.findByNameContaining(anyString(), any(PageRequest.class)))
                .thenThrow(new ApplicationNotFoundException("No applications found with the provided name"));

        assertThrows(ApplicationNotFoundException.class, () ->
                        applicationService.getApplicationsByName("Name", 0, 10),
                "Expected ApplicationNotFoundException to be thrown when no applications are found");

        verify(applicationRepository, times(1)).findByNameContaining(anyString(), any(PageRequest.class));
    }

    @Test
    void whenGetApplicationsByNameThrowsExceptionInvalidInput() {

        assertThrows(InvalidInputException.class, () ->
                        applicationService.getApplicationsByName(" ", 0, 10),
                "Name parameter must not be empty");

        verify(applicationRepository, never()).findByNameContaining(anyString(), any(PageRequest.class));
    }
    @Test
    public void whenGetApplicationsByNameThrowsExceptionDatabaseOperation() {

        when(applicationRepository.findByNameContaining(anyString(), any(PageRequest.class)))
                .thenThrow(new DataAccessException("Test exception") {});

        assertThrows(DatabaseOperationException.class, () ->
                applicationService.getApplicationsByName("Name", 0, 10));

        verify(applicationRepository, times(1)).findByNameContaining(anyString(), any(PageRequest.class));
    }
    @Test
    public void whenGetApplicationsByStatusWithValidInput() {

        Application app1 = new Application(1L, "Name1", "Content1", ApplicationStatus.CREATED, null, null, 0);
        Application app2 = new Application(2L, "Name2", "Content2", ApplicationStatus.CREATED, null, null, 0);
        List<Application> applications = Arrays.asList(app1, app2);

        Page<Application> expectedPage = new PageImpl<>(applications);

        when(applicationRepository.findByStatus(any(ApplicationStatus.class), any(PageRequest.class)))
                .thenReturn(expectedPage);

        Page<Application> resultPage = applicationService.getApplicationsByStatus(ApplicationStatus.CREATED, 0, 10);

        assertEquals(2, resultPage.getTotalElements());
        verify(applicationRepository, times(1)).findByStatus(any(ApplicationStatus.class), any(PageRequest.class));
    }
    @Test
    void whenGetApplicationsByStatusThrowsExceptionApplicationNotFound() {

        when(applicationRepository.findByStatus(any(ApplicationStatus.class), any(PageRequest.class)))
                .thenThrow(new ApplicationNotFoundException("No applications found with the provided status"));

        assertThrows(ApplicationNotFoundException.class, () ->
                        applicationService.getApplicationsByStatus(ApplicationStatus.CREATED, 0, 10),
                "Expected ApplicationNotFoundException to be thrown when no applications are found");

        verify(applicationRepository, times(1)).findByStatus(any(ApplicationStatus.class), any(PageRequest.class));
    }

    @Test
    public void whenGetApplicationsByStatusThrowsExceptionDatabaseOperation() {

        when(applicationRepository.findByStatus(any(ApplicationStatus.class), any(PageRequest.class)))
                .thenThrow(new DataAccessException("Test exception") {});

        assertThrows(DatabaseOperationException.class, () ->
                applicationService.getApplicationsByStatus(ApplicationStatus.CREATED, 0, 10));

        verify(applicationRepository, times(1)).findByStatus(any(ApplicationStatus.class), any(PageRequest.class));
    }

    @Test
    public void whenGetAllApplicationsThenReturnsPaginatedApplications() {

        Application app1 = new Application(1L, "Name1", "Content1", ApplicationStatus.CREATED, null, null, 0);
        Application app2 = new Application(2L, "Name2", "Content2", ApplicationStatus.CREATED, null, null, 0);
        List<Application> applications = Arrays.asList(app1, app2);

        Page<Application> expectedPage = new PageImpl<>(applications);

        when(applicationRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Application> result = applicationService.getAllApplications(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getTotalPages());

        verify(applicationRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    public void whenGetAllApplicationsThrowsExceptionDatabaseOperation() {

        when(applicationRepository.findAll(any(PageRequest.class)))
                .thenThrow(new DataAccessException("Test exception") {});

        assertThrows(DatabaseOperationException.class, () ->
                applicationService.getAllApplications(0, 10));

        verify(applicationRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    public void whenGetPaginatedApplicationsResponseWithFullPage() {

        List<ApplicationDTO> applicationList = Arrays.asList(new ApplicationDTO(), new ApplicationDTO());
        Page<ApplicationDTO> applicationPage = new PageImpl<>(applicationList);

        Map<String, Object> response = applicationService.getPaginatedApplicationsResponse(applicationPage);

        assertEquals(applicationList, response.get("applications"));
        assertEquals(0, response.get("currentPage"));
        assertEquals(2L, response.get("totalItems"));
        assertEquals(1, response.get("totalPages"));
    }
    @Test
    public void whenGetPaginatedApplicationsResponseWithEmptyList() {

        Page<ApplicationDTO> applicationPage = new PageImpl<>(Collections.emptyList());

        Map<String, Object> response = applicationService.getPaginatedApplicationsResponse(applicationPage);

        assertTrue(response.containsKey("applications"));
        assertTrue(response.containsKey("currentPage"));
        assertTrue(response.containsKey("totalItems"));
        assertTrue(response.containsKey("totalPages"));

        assertEquals(Collections.emptyList(), response.get("applications"));

        assertEquals(0, response.get("currentPage"));
        assertEquals(0L, response.get("totalItems"));
        assertEquals(1, response.get("totalPages"));
    }

    @Test
    public void whenGetPaginatedApplicationsResponseWithLargeDataset() {

        long totalElements = 100;
        List<ApplicationDTO> applications = new ArrayList<>();
        for (int i = 0; i < totalElements; i++) {
            applications.add(new ApplicationDTO());}

                int page = 5;
                int size = 10;
                Page<ApplicationDTO> applicationPage = new PageImpl<>(applications, PageRequest.of(page, size), totalElements);

                Map<String, Object> response = applicationService.getPaginatedApplicationsResponse(applicationPage);

                assertNotNull(response.get("applications"));
                assertEquals(page, response.get("currentPage"));
                assertEquals(totalElements, response.get("totalItems"));
                assertEquals((int) Math.ceil((double) totalElements / size), response.get("totalPages"));

        }
    }