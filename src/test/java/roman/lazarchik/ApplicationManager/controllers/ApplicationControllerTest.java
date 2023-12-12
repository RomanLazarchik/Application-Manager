package roman.lazarchik.ApplicationManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roman.lazarchik.ApplicationManager.dto.RejectDeleteDTO;
import roman.lazarchik.ApplicationManager.dto.UpdateContentDTO;
import roman.lazarchik.ApplicationManager.exceptions.*;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.services.ApplicationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;


    @Test
    public void testCreateApplicationSuccess() throws Exception {
        Application app = new Application();
        app.setName("Test App");
        app.setContent("Test Content");

        when(applicationService.createApplication(any(Application.class))).thenReturn(app);

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(app)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(app.getName()))
                .andExpect(jsonPath("$.content").value(app.getContent()));

        verify(applicationService, times(1)).createApplication(any(Application.class));
    }

    @Test
    public void testCreateApplicationInvalidInput() throws Exception {
        Application app = new Application();
        app.setName("Test App");

        when(applicationService.createApplication(any(Application.class)))
                .thenThrow(new InvalidInputException("Fields 'name' and 'content' must not be null or empty"));

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(app)))
                .andExpect(status().isBadRequest());

        verify(applicationService, times(1)).createApplication(any(Application.class));
    }

    @Test
    public void testCreateApplicationThrowsException() throws Exception {
        Application app = new Application();
        app.setName("Test App");
        app.setContent("Test Content");

        when(applicationService.createApplication(any(Application.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(app)))
                .andExpect(status().isBadRequest());

        verify(applicationService, times(1)).createApplication(any(Application.class));
    }

    @Test
    void updateContent_Success() throws Exception {
        UpdateContentDTO updateContentDTO = new UpdateContentDTO("Updated Content");
        Application updatedApplication = new Application();
        updatedApplication.setId(1L);
        updatedApplication.setName("Example Application Name");
        updatedApplication.setContent("Updated Content");
        updatedApplication.setStatus(ApplicationStatus.CREATED);

        Mockito.when(applicationService.updateContent(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(updatedApplication);

        mockMvc.perform(put("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    void updateContent_ApplicationNotFound() throws Exception {
        UpdateContentDTO updateContentDTO = new UpdateContentDTO("Some Content");
        Long invalidId = 999L;

        Mockito.when(applicationService.updateContent(Mockito.eq(invalidId), Mockito.anyString()))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + invalidId));

        mockMvc.perform(put("/applications/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContentDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateContent_ContentEditNotAllowed() throws Exception {
        UpdateContentDTO updateContentDTO = new UpdateContentDTO("Some Content");
        Long someId = 1L;

        Mockito.when(applicationService.updateContent(Mockito.eq(someId), Mockito.anyString()))
                .thenThrow(new ContentEditNotAllowedException("Cannot edit content in this status"));

        mockMvc.perform(put("/applications/" + someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateContent_InvalidInput() throws Exception {
        UpdateContentDTO updateContentDTO = new UpdateContentDTO("");
        Long someId = 1L;

        mockMvc.perform(put("/applications/" + someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteApplicationSuccessfully() throws Exception {
        RejectDeleteDTO deleteDTO = new RejectDeleteDTO("Valid reason");

        doNothing().when(applicationService).deleteApplication(any(Long.class), any(RejectDeleteDTO.class));

        mockMvc.perform(delete("/applications/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(deleteDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldHandleApplicationNotFoundException() throws Exception {
        long invalidId = 999L;
        RejectDeleteDTO deleteDTO = new RejectDeleteDTO("Valid reason");

        doThrow(new ApplicationNotFoundException("Application not found with ID: " + invalidId))
                .when(applicationService).deleteApplication(eq(invalidId), any(RejectDeleteDTO.class));

        mockMvc.perform(delete("/applications/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(deleteDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldValidateRejectDeleteDTO() throws Exception {
        long validId = 1L;
        RejectDeleteDTO invalidDeleteDTO = new RejectDeleteDTO("");

        mockMvc.perform(delete("/applications/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidDeleteDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldHandleInvalidApplicationStatusException() throws Exception {
        long validId = 1L;
        RejectDeleteDTO deleteDTO = new RejectDeleteDTO("Valid reason");

        doThrow(new InvalidApplicationStatusException("Application can only be deleted in the CREATED status."))
                .when(applicationService).deleteApplication(eq(validId), any(RejectDeleteDTO.class));

        mockMvc.perform(delete("/applications/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(deleteDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldVerifyApplicationSuccessfully() throws Exception {
        Long applicationId = 1L;
        Application verifiedApp = new Application();
        verifiedApp.setId(applicationId);
        verifiedApp.setStatus(ApplicationStatus.VERIFIED);

        Mockito.when(applicationService.verifyApplication(applicationId)).thenReturn(verifiedApp);

        mockMvc.perform(put("/applications/" + applicationId + "/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("VERIFIED")));
    }

    @Test
    public void shouldFailVerificationDueToStatus() throws Exception {
        Long applicationId = 1L;
        Mockito.when(applicationService.verifyApplication(applicationId))
                .thenThrow(new InvalidApplicationStatusException("Invalid status for verification"));

        mockMvc.perform(put("/applications/" + applicationId + "/verify"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid status for verification")));
    }

    @Test
    public void shouldFailVerificationDueToApplicationNotFound() throws Exception {
        Long applicationId = 1L;
        Mockito.when(applicationService.verifyApplication(applicationId))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + applicationId));

        mockMvc.perform(put("/applications/" + applicationId + "/verify"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Application not found with ID: " + applicationId)));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRejectApplication_Success() throws Exception {
        Long applicationId = 1L;
        RejectDeleteDTO rejectDTO = new RejectDeleteDTO("Some valid reason");
        Application mockApplication = new Application();
        mockApplication.setId(1L);
        mockApplication.setName("Test Application");
        mockApplication.setContent("Test Content");
        mockApplication.setStatus(ApplicationStatus.CREATED);
        mockApplication.setReason(null);

        Mockito.when(applicationService.rejectApplication(applicationId, rejectDTO)).thenReturn(mockApplication);

        mockMvc.perform(put("/applications/" + applicationId + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rejectDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testRejectApplication_NotFound() throws Exception {
        Long applicationId = 1L;
        RejectDeleteDTO rejectDTO = new RejectDeleteDTO("Valid reason");

        Mockito.when(applicationService.rejectApplication(applicationId, rejectDTO))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + applicationId));

        mockMvc.perform(put("/applications/" + applicationId + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rejectDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRejectApplication_ValidationFailed() throws Exception {
        Long applicationId = 1L;
        RejectDeleteDTO rejectDTO = new RejectDeleteDTO("");

        mockMvc.perform(put("/applications/" + applicationId + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rejectDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void acceptApplication_Success() throws Exception {
        Application app = new Application();
        app.setId(1L);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationService.acceptApplication(1L)).thenReturn(app);

        mockMvc.perform(put("/applications/1/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).acceptApplication(1L);
    }

    @Test
    public void acceptApplication_NotFound() throws Exception {
        when(applicationService.acceptApplication(1L)).thenThrow(new ApplicationNotFoundException("Application not found with ID: 1"));

        mockMvc.perform(put("/applications/1/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void acceptApplication_InvalidStatus() throws Exception {
        when(applicationService.acceptApplication(1L)).thenThrow(new InvalidApplicationStatusException("Application can only be accepted in the VERIFIED status."));

        mockMvc.perform(put("/applications/1/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenPublishApplication_thenStatusIsOk() throws Exception {
        Application publishedApp = new Application();
        publishedApp.setId(1L);
        publishedApp.setName("Test Application");
        publishedApp.setContent("Test Content");
        publishedApp.setStatus(ApplicationStatus.PUBLISHED);

        Mockito.when(applicationService.publishApplication(1L)).thenReturn(publishedApp);

        mockMvc.perform(put("/applications/1/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(ApplicationStatus.PUBLISHED.toString())));
    }

    @Test
    public void whenPublishNonExistentApplication_thenThrowApplicationNotFoundException() throws Exception {
        Mockito.when(applicationService.publishApplication(1L))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: 1"));

        mockMvc.perform(put("/applications/1/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.details", is("Application not found")));
    }

    @Test
    public void whenPublishAlreadyPublishedApplication_thenThrowApplicationAlreadyPublishedException() throws Exception {
        Mockito.when(applicationService.publishApplication(1L))
                .thenThrow(new ApplicationAlreadyPublishedException("Application is already published"));

        mockMvc.perform(put("/applications/1/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationAlreadyPublishedException))
                .andExpect(jsonPath("$.details", is("Application Already Published")));
    }

    @Test
    public void whenPublishApplication_thenHistoryIsUpdated() throws Exception {
        Application publishedApp = new Application();
        publishedApp.setId(1L);
        publishedApp.setName("Test Application");
        publishedApp.setContent("Test Content");
        publishedApp.setStatus(ApplicationStatus.PUBLISHED);

        Mockito.when(applicationService.publishApplication(1L)).thenReturn(publishedApp);

        mockMvc.perform(put("/applications/1/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetApplications() throws Exception {
        Application application = new Application();
        application.setId(1L);
        application.setName("Test Application");
        application.setContent("This is a test application content.");
        application.setStatus(ApplicationStatus.CREATED);
        application.setReason("Initial Creation");
        // application.setHistories(...)
        application.setPublishedNumber(0);
        Page<Application> applications = new PageImpl<>(Collections.singletonList(application));

        when(applicationService.getApplications(anyString(), any(), anyInt(), anyInt())).thenReturn(applications);

        mockMvc.perform(get("/applications")
                        .param("name", "testName")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").exists());

        verify(applicationService, times(1)).getApplications(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    public void testGetApplicationsWithEmptyResult() throws Exception {
        Page<Application> emptyApplications = new PageImpl<>(Collections.emptyList());

        when(applicationService.getApplications(anyString(), any(), anyInt(), anyInt())).thenReturn(emptyApplications);

        mockMvc.perform(get("/applications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(applicationService, times(1)).getApplications(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    public void testGetApplicationsThrowsException() throws Exception {
        when(applicationService.getApplications(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new InvalidApplicationStatusException("Invalid status"));

        mockMvc.perform(get("/applications")
                        .param("status", "CREATED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status"));

        verify(applicationService, times(1)).getApplications(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    public void testGetApplicationsWithPagination() throws Exception {
        List<Application> applications = Arrays.asList(new Application(), new Application());
        Page<Application> applicationsPage = new PageImpl<>(applications, PageRequest.of(1, 2), applications.size());

        when(applicationService.getApplications(anyString(), any(), anyInt(), anyInt())).thenReturn(applicationsPage);

        mockMvc.perform(get("/applications")
                        .param("page", "1")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageable.pageNumber").value("1"))
                .andExpect(jsonPath("$.pageable.pageSize").value("2"));

        verify(applicationService, times(1)).getApplications(anyString(), any(), anyInt(), anyInt());
    }
}