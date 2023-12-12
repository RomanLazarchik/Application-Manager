package roman.lazarchik.ApplicationManager.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import roman.lazarchik.ApplicationManager.dto.RejectDeleteDTO;
import roman.lazarchik.ApplicationManager.exceptions.*;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.repositories.ApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private ApplicationHistoryService applicationHistoryService;

    @Test
    void whenCreateApplicationWithValidInput_thenApplicationIsCreated() {

        Application app = new Application();
        app.setName("Test App");
        app.setContent("Test Content");
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        Application createdApp = applicationService.createApplication(app);

        assertEquals("Test App", createdApp.getName());
        assertEquals("Test Content", createdApp.getContent());
        Mockito.verify(applicationRepository).save(any(Application.class));
    }


    @Test
    void whenCreateApplication_thenHistoryIsSaved() {

        Application app = new Application();
        app.setName("Test App");
        app.setContent("Test Content");

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        applicationService.createApplication(app);

        Mockito.verify(applicationHistoryService).saveHistory(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();
        assertEquals(ApplicationStatus.CREATED, capturedHistory.getStatus());
        assertNotNull(capturedHistory.getTimestamp());
    }

    @Test
    void whenCreateApplicationWithInvalidData_thenThrowInvalidInputException() {

        Application app = new Application();
        app.setName("");
        app.setContent(" ");

        assertThrows(InvalidInputException.class, () -> {
            applicationService.createApplication(app);
        });
    }

    @Test
    void whenCreateApplicationWithMissingName_thenThrowInvalidInputException() {

        Application app = new Application();
        app.setContent("Test Content");

        assertThrows(InvalidInputException.class, () -> {
            applicationService.createApplication(app);
        });
    }

    @Test
    void whenCreateApplicationWithMissingContent_thenThrowInvalidInputException() {

        Application app = new Application();
        app.setName("Test App");

        assertThrows(InvalidInputException.class, () -> {
            applicationService.createApplication(app);
        });
    }

    @Test
    public void updateContent_SuccessfulUpdate_ReturnsUpdatedApplication() {

        Long applicationId = 1L;
        String newContent = "New Content";
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setContent("Old Content");
        existingApplication.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApplication);

        Application updatedApplication = applicationService.updateContent(applicationId, newContent);

        assertThat(updatedApplication.getContent()).isEqualTo(newContent);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    public void updateContent_ApplicationNotFound_ThrowsException() {
        Long applicationId = 1L;
        String newContent = "New Content";

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.updateContent(applicationId, newContent);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void updateContent_ContentEditNotAllowed_ThrowsException() {
        Long applicationId = 1L;
        String newContent = "New Content";
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setContent("Old Content");
        existingApplication.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));

        assertThrows(ContentEditNotAllowedException.class, () -> {
            applicationService.updateContent(applicationId, newContent);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void rejectApplication_SuccessfulRejection_ReturnsRejectedApplication() {

        Long applicationId = 1L;
        RejectDeleteDTO rejectDTO = new RejectDeleteDTO("Valid Reason");
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApplication);

        Application rejectedApplication = applicationService.rejectApplication(applicationId, rejectDTO);

        assertThat(rejectedApplication.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(rejectedApplication.getReason()).isEqualTo(rejectDTO.getReason());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void rejectApplication_ApplicationNotFound_ThrowsException() {
        Long applicationId = 1L;
        RejectDeleteDTO rejectDTO = new RejectDeleteDTO("Valid Reason");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.rejectApplication(applicationId, rejectDTO);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void rejectApplication_InvalidApplicationStatus_ThrowsException() {
        Long applicationId = 1L;
        RejectDeleteDTO rejectDTO = new RejectDeleteDTO("Valid Reason");
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));

        assertThrows(InvalidApplicationStatusException.class, () -> {
            applicationService.rejectApplication(applicationId, rejectDTO);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void testDeleteApplication_Success() {
        Long applicationId = 1L;
        Application app = new Application();
        app.setId(applicationId);
        app.setStatus(ApplicationStatus.CREATED);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));

        applicationService.deleteApplication(applicationId, new RejectDeleteDTO("Test reason"));

        assertEquals(ApplicationStatus.DELETED, app.getStatus());
        verify(applicationRepository).save(app);
    }

    @Test
    public void testDeleteApplication_NotFound() {
        Long applicationId = 1L;
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () ->
                applicationService.deleteApplication(applicationId, new RejectDeleteDTO("Test reason"))
        );
    }

    @Test
    public void testDeleteApplication_InvalidStatus() {
        Long applicationId = 1L;
        Application app = new Application();
        app.setId(applicationId);
        app.setStatus(ApplicationStatus.PUBLISHED);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));

        assertThrows(InvalidApplicationStatusException.class, () ->
                applicationService.deleteApplication(applicationId, new RejectDeleteDTO("Test reason"))
        );
    }

    @Test
    void verifyApplication_SuccessfulVerification_ReturnsVerifiedApplication() {

        Long applicationId = 1L;
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApplication);

        Application verifiedApplication = applicationService.verifyApplication(applicationId);

        assertThat(verifiedApplication.getStatus()).isEqualTo(ApplicationStatus.VERIFIED);
        verify(applicationRepository).save(any(Application.class));
        verify(applicationHistoryService).saveHistory(any(ApplicationHistory.class));
    }

    @Test
    void verifyApplication_ApplicationNotFound_ThrowsException() {
        Long applicationId = 1L;

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.verifyApplication(applicationId);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void verifyApplication_InvalidApplicationStatus_ThrowsException() {
        Long applicationId = 1L;
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));

        assertThrows(InvalidApplicationStatusException.class, () -> {
            applicationService.verifyApplication(applicationId);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void acceptApplication_SuccessfulAcceptance_ReturnsAcceptedApplication() {

        Long applicationId = 1L;
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setStatus(ApplicationStatus.VERIFIED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApplication);

        Application acceptedApplication = applicationService.acceptApplication(applicationId);

        assertThat(acceptedApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void acceptApplication_ApplicationNotFound_ThrowsException() {
        Long applicationId = 1L;

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.acceptApplication(applicationId);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void acceptApplication_InvalidApplicationStatus_ThrowsException() {
        Long applicationId = 1L;
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));

        assertThrows(InvalidApplicationStatusException.class, () -> {
            applicationService.acceptApplication(applicationId);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    public void testPublishApplication_Success() {
        Long applicationId = 1L;
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.findMaxPublishedNumber()).thenReturn(Optional.of(10));
        when(applicationRepository.save(any(Application.class))).then(returnsFirstArg());

        Application publishedApplication = applicationService.publishApplication(applicationId);

        assertNotNull(publishedApplication);
        assertEquals(ApplicationStatus.PUBLISHED, publishedApplication.getStatus());
        assertEquals(11, publishedApplication.getPublishedNumber().intValue());

        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    public void testPublishApplication_AlreadyPublishedException() {
        Long applicationId = 1L;
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.PUBLISHED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThrows(ApplicationAlreadyPublishedException.class, () -> {
            applicationService.publishApplication(applicationId);
        });
    }

    @Test
    public void testPublishApplication_NotFoundException() {
        Long applicationId = 1L;

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.publishApplication(applicationId);
        });
    }

    @Test
    public void testPublishApplication_RepositoryInteraction() {
        Long applicationId = 1L;
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.findMaxPublishedNumber()).thenReturn(Optional.of(10));
        when(applicationRepository.save(any(Application.class))).then(returnsFirstArg());

        applicationService.publishApplication(applicationId);

        verify(applicationRepository).findMaxPublishedNumber();
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void whenUpdateApplicationContent_thenHistoryIsSaved() {
        Long applicationId = 1L;
        String newContent = "New Content";
        Application existingApplication = new Application();
        existingApplication.setId(applicationId);
        existingApplication.setContent("Old Content");
        existingApplication.setStatus(ApplicationStatus.CREATED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(existingApplication);

        ArgumentCaptor<ApplicationHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationHistory.class);

        applicationService.updateContent(applicationId, newContent);

        verify(applicationHistoryService).saveHistory(historyCaptor.capture());
        ApplicationHistory capturedHistory = historyCaptor.getValue();

        assertEquals(ApplicationStatus.CREATED, capturedHistory.getStatus());
        assertEquals(existingApplication, capturedHistory.getApplication());
        assertNotNull(capturedHistory.getTimestamp());
    }
}