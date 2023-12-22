package roman.lazarchik.ApplicationManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import roman.lazarchik.ApplicationManager.dto.ApplicationDTO;
import roman.lazarchik.ApplicationManager.dto.DeleteDTO;
import roman.lazarchik.ApplicationManager.dto.RejectDTO;
import roman.lazarchik.ApplicationManager.dto.UpdateContentDTO;
import roman.lazarchik.ApplicationManager.exceptions.*;
import roman.lazarchik.ApplicationManager.mapper.ApplicationMapper;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.services.ApplicationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private ApplicationMapper mapper;


    @Test
    void createApplication_Success() throws Exception {
        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setName("Name");
        appDTO.setContent("Content");

        Application createApp = new Application();
        createApp.setId(1L);
        createApp.setName(appDTO.getName());
        createApp.setContent(appDTO.getContent());
        createApp.setStatus(ApplicationStatus.CREATED);

        ApplicationDTO mockApplicationDTO = new ApplicationDTO();
        mockApplicationDTO.setId(createApp.getId());
        mockApplicationDTO.setName(createApp.getName());
        mockApplicationDTO.setContent(createApp.getContent());
        mockApplicationDTO.setStatus(ApplicationStatus.CREATED);

        when(applicationService.createApplication(mapper.toEntity(appDTO))).thenReturn(createApp);
        when(mapper.toDto(createApp)).thenReturn(mockApplicationDTO);

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockApplicationDTO.getId()))
                .andExpect(jsonPath("$.name").value(mockApplicationDTO.getName()))
                .andExpect(jsonPath("$.content").value(mockApplicationDTO.getContent()))
                .andExpect(jsonPath("$.status").value(ApplicationStatus.CREATED.toString()));

        verify(applicationService, times(1)).createApplication(any());
    }

    @Test
    void createApplication_InvalidInput() throws Exception {
        Application app = new Application();
        app.setName(" ");
        app.setContent("Content");

        when(applicationService.createApplication(any()))
                .thenThrow(new InvalidInputException("Fields 'name' and 'content' must not be null or empty"));

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(app)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
                .andExpect(jsonPath("$.message", is("Fields 'name' and 'content' must not be null or empty")));

        verify(applicationService, times(1)).createApplication(any());
    }

    @Test
    void updateContent_Success() throws Exception {
        UpdateContentDTO updateContent = new UpdateContentDTO("Updated Content");
        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setName("Name");
        app.setContent(updateContent.getContent());
        app.setStatus(ApplicationStatus.CREATED);

        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setId(app.getId());
        appDTO.setName(app.getName());
        appDTO.setContent(app.getContent());
        appDTO.setStatus(app.getStatus());

        when(applicationService.updateContent(someId, updateContent.getContent())).thenReturn(app);
        when(mapper.toDto(app)).thenReturn(appDTO);

        mockMvc.perform(put("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContent)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(updateContent.getContent()));

        verify(applicationService, times(1)).updateContent(someId, updateContent.getContent());
    }

    @Test
    void updateContent_ApplicationNotFound() throws Exception {
        UpdateContentDTO updateContent = new UpdateContentDTO("Updated Content");
        long someId = 999L;

        when(applicationService.updateContent(someId, updateContent.getContent()))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + someId));

        mockMvc.perform(put("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContent)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message", is("Application not found with ID: " + someId)));

        verify(applicationService, times(1)).updateContent(someId, updateContent.getContent());
    }

    @Test
    void updateContent_ContentEditNotAllowed() throws Exception {
        UpdateContentDTO updateContent = new UpdateContentDTO("Updated Content");
        long someId = 1L;

        when(applicationService.updateContent(someId, updateContent.getContent()))
                .thenThrow(new ContentEditNotAllowedException("Cannot edit content in this status. " +
                        "Content can only be edited in CREATED or VERIFIED status."));

        mockMvc.perform(put("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ContentEditNotAllowedException))
                .andExpect(jsonPath("$.message", is("Cannot edit content in this status. " +
                        "Content can only be edited in CREATED or VERIFIED status.")));

        verify(applicationService, times(1)).updateContent(someId, updateContent.getContent());
    }

    @Test
    void updateContent_ValidateUpdateContentDTO() throws Exception {
        UpdateContentDTO updateContent = new UpdateContentDTO(" ");
        long someId = 1L;

        mockMvc.perform(put("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateContent)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Validation Error")));

    }

    @Test
    void deleteApplication_Success() throws Exception {
        DeleteDTO reasonDelete = new DeleteDTO("Reason");
        long someId = 1L;

        doNothing().when(applicationService).deleteApplication(someId, reasonDelete);

        mockMvc.perform(delete("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonDelete)))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).deleteApplication(someId, reasonDelete);
    }

    @Test
    void deleteApplication_ApplicationNotFoundException() throws Exception {
        DeleteDTO reasonDelete = new DeleteDTO("Reason");
        long someId = 999L;

        doThrow(new ApplicationNotFoundException("Application not found with ID: " + someId))
                .when(applicationService).deleteApplication(someId, reasonDelete);

        mockMvc.perform(delete("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonDelete)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message", is("Application not found with ID: " + someId)));

        verify(applicationService, times(1)).deleteApplication(someId, reasonDelete);
    }

    @Test
    void deleteApplication_ValidateDeleteDTO() throws Exception {
        DeleteDTO reasonDelete = new DeleteDTO(" ");
        long someId = 1L;

        mockMvc.perform(delete("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonDelete)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Validation Error")));
    }

    @Test
    void deleteApplication_InvalidApplicationStatusException() throws Exception {
        DeleteDTO reasonDelete = new DeleteDTO("Reason");
        long someId = 1L;

        doThrow(new InvalidApplicationStatusException("Application can only be deleted in the CREATED status."))
                .when(applicationService).deleteApplication(someId, reasonDelete);

        mockMvc.perform(delete("/applications/{id}", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonDelete)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidApplicationStatusException))
                .andExpect(jsonPath("$.message", is("Application can only be deleted in the CREATED status.")));

        verify(applicationService, times(1)).deleteApplication(someId, reasonDelete);

    }

    @Test
    void verifyApplication_Success() throws Exception {
        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setName("Name");
        app.setContent("Content");
        app.setStatus(ApplicationStatus.VERIFIED);

        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setId(app.getId());
        appDTO.setName(app.getName());
        appDTO.setContent(app.getContent());
        appDTO.setStatus(app.getStatus());

        when(applicationService.verifyApplication(someId)).thenReturn(app);
        when(mapper.toDto(app)).thenReturn(appDTO);

        mockMvc.perform(put("/applications/{id}/verify", someId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(ApplicationStatus.VERIFIED.toString())));

        verify(applicationService, times(1)).verifyApplication(someId);
    }

    @Test
    void verifyApplication_InvalidApplicationStatusException() throws Exception {
        long someId = 1L;

        when(applicationService.verifyApplication(someId))
                .thenThrow(new InvalidApplicationStatusException("Application can only be verified in the CREATED status."));

        mockMvc.perform(put("/applications/{id}/verify", someId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidApplicationStatusException))
                .andExpect(jsonPath("$.message", is("Application can only be verified in the CREATED status.")));

        verify(applicationService, times(1)).verifyApplication(someId);
    }

    @Test
    void verifyApplication_ApplicationNotFoundException() throws Exception {
        long someId = 1L;

        when(applicationService.verifyApplication(someId))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + someId));

        mockMvc.perform(put("/applications/{id}/verify", someId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message", is("Application not found with ID: " + someId)));

        verify(applicationService, times(1)).verifyApplication(someId);
    }

    @Test
    void rejectApplication_Success() throws Exception {
        RejectDTO reasonReject = new RejectDTO("Reason");
        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setName("Name");
        app.setContent("Content");
        app.setStatus(ApplicationStatus.REJECTED);
        app.setReason(reasonReject.getReason());

        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setId(app.getId());
        appDTO.setName(app.getName());
        appDTO.setContent(app.getContent());
        appDTO.setStatus(app.getStatus());
        appDTO.setReason(app.getReason());

        when(applicationService.rejectApplication(someId, reasonReject)).thenReturn(app);
        when(mapper.toDto(app)).thenReturn(appDTO);

        mockMvc.perform(put("/applications/{id}/reject", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonReject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason", is(reasonReject.getReason())));

        verify(applicationService, times(1)).rejectApplication(someId, reasonReject);
    }

    @Test
    void rejectApplication_ApplicationNotFoundException() throws Exception {
        RejectDTO reasonReject = new RejectDTO("Reason");
        long someId = 1L;

        when(applicationService.rejectApplication(someId, reasonReject))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + someId));

        mockMvc.perform(put("/applications/{id}/reject", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonReject)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message", is("Application not found with ID: " + someId)));

        verify(applicationService, times(1)).rejectApplication(someId, reasonReject);
    }

    @Test
    void rejectApplication_InvalidApplicationStatusException() throws Exception {
        RejectDTO reasonReject = new RejectDTO("Reason");
        long someId = 1L;

        when(applicationService.rejectApplication(someId, reasonReject))
                .thenThrow(new InvalidApplicationStatusException("Can only reject applications with status VERIFIED or ACCEPTED"));

        mockMvc.perform(put("/applications/{id}/reject", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonReject)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidApplicationStatusException))
                .andExpect(jsonPath("$.message", is("Can only reject applications with status VERIFIED or ACCEPTED")));

        verify(applicationService, times(1)).rejectApplication(someId, reasonReject);

    }

    @Test
    void rejectApplication_ValidateRejectDTO() throws Exception {
        RejectDTO reasonReject = new RejectDTO("");
        long someId = 1L;

        mockMvc.perform(put("/applications/{id}/reject", someId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(reasonReject)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.message", is("Validation Error")));

    }

    @Test
    void acceptApplication_Success() throws Exception {
        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setName("Name");
        app.setContent("Content");
        app.setStatus(ApplicationStatus.ACCEPTED);

        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setId(app.getId());
        appDTO.setName(app.getName());
        appDTO.setContent(app.getContent());
        appDTO.setStatus(app.getStatus());

        when(applicationService.acceptApplication(someId)).thenReturn(app);
        when(mapper.toDto(app)).thenReturn(appDTO);

        mockMvc.perform(put("/applications/{id}/accept", someId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(ApplicationStatus.ACCEPTED.toString())));

        verify(applicationService, times(1)).acceptApplication(someId);
    }

    @Test
    void acceptApplication_ApplicationNotFoundException() throws Exception {
        long someId = 1L;

        when(applicationService.acceptApplication(someId))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + someId));

        mockMvc.perform(put("/applications/{id}/accept", someId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message").value("Application not found with ID: " + someId))
                .andExpect(jsonPath("$.details").value("Application not found"));

        verify(applicationService, times(1)).acceptApplication(someId);
    }

    @Test
    void acceptApplication_InvalidApplicationStatusException() throws Exception {
        long someId = 1L;

        when(applicationService.acceptApplication(someId))
                .thenThrow(new InvalidApplicationStatusException("Application can only be accepted in the VERIFIED status"));

        mockMvc.perform(put("/applications/{id}/accept", someId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidApplicationStatusException))
                .andExpect(jsonPath("$.message").value("Application can only be accepted in the VERIFIED status"))
                .andExpect(jsonPath("$.details").value("Invalid Application Status"));
    }

    @Test
    void publishApplication_Success() throws Exception {
        long someId = 1L;

        Application app = new Application();
        app.setId(someId);
        app.setName("Name");
        app.setContent("Content");
        app.setStatus(ApplicationStatus.PUBLISHED);

        ApplicationDTO appDTO = new ApplicationDTO();
        appDTO.setId(app.getId());
        appDTO.setName(app.getName());
        appDTO.setContent(app.getContent());
        appDTO.setStatus(app.getStatus());

        when(applicationService.publishApplication(someId)).thenReturn(app);
        when(mapper.toDto(app)).thenReturn(appDTO);

        mockMvc.perform(put("/applications/{id}/publish", someId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(ApplicationStatus.PUBLISHED.toString())));
    }

    @Test
    void publishApplication_ApplicationNotFoundException() throws Exception {
        long someId = 1L;

        when(applicationService.publishApplication(someId))
                .thenThrow(new ApplicationNotFoundException("Application not found with ID: " + someId));

        mockMvc.perform(put("/applications/{id}/publish", someId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message", is("Application not found with ID: " + someId)))
                .andExpect(jsonPath("$.details", is("Application not found")));
    }

    @Test
    void publishApplication_InvalidApplicationStatusException() throws Exception {
        long someId = 1L;

        when(applicationService.publishApplication(someId))
                .thenThrow(new InvalidApplicationStatusException("Application can only be published in the ACCEPTED status"));

        mockMvc.perform(put("/applications/{id}/publish", someId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidApplicationStatusException))
                .andExpect(jsonPath("$.message", is("Application can only be published in the ACCEPTED status")))
                .andExpect(jsonPath("$.details", is("Invalid Application Status")));
    }

    @Test
    void getApplicationsWithAllParams() throws Exception {
        Application application = new Application();
        application.setId(1L);
        application.setName("Name");
        application.setContent("Content");
        application.setStatus(ApplicationStatus.CREATED);
        application.setReason(null);
        application.setPublishedNumber(null);

        Page<Application> applications = new PageImpl<>(Collections.singletonList(application));

        Page<ApplicationDTO> dtoPage = applications.map(mapper::toDto);

        Map<String, Object> response = Map.of(
                "applications", dtoPage.getContent(),
                "currentPage", dtoPage.getNumber(),
                "totalItems", dtoPage.getTotalElements(),
                "totalPages", dtoPage.getTotalPages()
        );

        when(applicationService.getApplicationsByNameAndStatus(anyString(), any(), anyInt(), anyInt())).thenReturn(applications);
        when(applicationService.getPaginatedApplicationsResponse(dtoPage)).thenReturn(response);

        mockMvc.perform(get("/applications")
                        .param("name", "Name")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(applicationService, times(1)).getApplicationsByNameAndStatus(anyString(), any(), anyInt(), anyInt());
        verify(applicationService, times(1)).getPaginatedApplicationsResponse(dtoPage);
    }

    @Test
    void getApplicationsWithAllParams_InvalidInputName() throws Exception {
        when(applicationService.getApplicationsByNameAndStatus(eq(" "), any(), anyInt(), anyInt()))
                .thenThrow(new InvalidInputException("Name parameter must not be null or empty"));

        mockMvc.perform(get("/applications")
                        .param("status", "CREATED")
                        .param("name", " ")
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
                .andExpect(jsonPath("$.message").value("Name parameter must not be null or empty"))
                .andExpect(jsonPath("$.details").value("Invalid Input"));

        verify(applicationService, times(1)).getApplicationsByNameAndStatus(eq(" "), any(), anyInt(), anyInt());
    }

    @Test
    void getApplicationsWithAllParams_ApplicationNotFound() throws Exception {
        when(applicationService.getApplicationsByNameAndStatus(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new ApplicationNotFoundException("No applications found with the provided name and status"));

        mockMvc.perform(get("/applications")
                        .param("status", "CREATED")
                        .param("name", "Name")
                        .param("page", "1")
                        .param("size", "15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApplicationNotFoundException))
                .andExpect(jsonPath("$.message").value("No applications found with the provided name and status"))
                .andExpect(jsonPath("$.details").value("Application not found"));

        verify(applicationService, times(1)).getApplicationsByNameAndStatus(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    void getApplicationsWithNameParameter() throws Exception {
        Application application = new Application();
        application.setId(1L);
        application.setName("Name");
        application.setContent("Content");
        application.setStatus(ApplicationStatus.CREATED);
        application.setReason(null);
        application.setPublishedNumber(null);

        Page<Application> applications = new PageImpl<>(Collections.singletonList(application));

        Page<ApplicationDTO> dtoPage = applications.map(mapper::toDto);

        Map<String, Object> response = Map.of(
                "applications", dtoPage.getContent(),
                "currentPage", dtoPage.getNumber(),
                "totalItems", dtoPage.getTotalElements(),
                "totalPages", dtoPage.getTotalPages()
        );

        when(applicationService.getApplicationsByName(anyString(), anyInt(), anyInt())).thenReturn(applications);
        when(applicationService.getPaginatedApplicationsResponse(dtoPage)).thenReturn(response);

        mockMvc.perform(get("/applications")
                        .param("name", "Name")
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(applicationService, times(1)).getApplicationsByName(anyString(), anyInt(), anyInt());
        verify(applicationService, times(1)).getPaginatedApplicationsResponse(dtoPage);
    }

    @Test
    void getApplicationsWithStatusParameter() throws Exception {
        Application application = new Application();
        application.setId(1L);
        application.setName("Name");
        application.setContent("Content");
        application.setStatus(ApplicationStatus.CREATED);
        application.setReason(null);
        application.setPublishedNumber(null);

        Page<Application> applications = new PageImpl<>(Collections.singletonList(application));

        Page<ApplicationDTO> dtoPage = applications.map(mapper::toDto);

        Map<String, Object> response = Map.of(
                "applications", dtoPage.getContent(),
                "currentPage", dtoPage.getNumber(),
                "totalItems", dtoPage.getTotalElements(),
                "totalPages", dtoPage.getTotalPages()
        );

        when(applicationService.getApplicationsByStatus(any(), anyInt(), anyInt())).thenReturn(applications);
        when(applicationService.getPaginatedApplicationsResponse(dtoPage)).thenReturn(response);

        mockMvc.perform(get("/applications")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(applicationService, times(1)).getApplicationsByStatus(any(), anyInt(), anyInt());
        verify(applicationService, times(1)).getPaginatedApplicationsResponse(dtoPage);
    }

    @Test
    void getApplicationsWithoutNameAndStatus() throws Exception {
        Application application = new Application();
        application.setId(1L);
        application.setName("Name");
        application.setContent("Content");
        application.setStatus(ApplicationStatus.CREATED);
        application.setReason(null);
        application.setPublishedNumber(null);

        Page<Application> applications = new PageImpl<>(Collections.singletonList(application));

        Page<ApplicationDTO> dtoPage = applications.map(mapper::toDto);

        Map<String, Object> response = Map.of(
                "applications", dtoPage.getContent(),
                "currentPage", dtoPage.getNumber(),
                "totalItems", dtoPage.getTotalElements(),
                "totalPages", dtoPage.getTotalPages()
        );

        when(applicationService.getAllApplications(anyInt(), anyInt())).thenReturn(applications);
        when(applicationService.getPaginatedApplicationsResponse(dtoPage)).thenReturn(response);

        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));


        verify(applicationService, times(1)).getAllApplications(anyInt(), anyInt());
        verify(applicationService, times(1)).getPaginatedApplicationsResponse(dtoPage);
    }

    @Test
    void getApplicationsWithPagination() throws Exception {
        List<Application> applicationsList = Arrays.asList(new Application(), new Application());

        Page<Application> applicationsPage = new PageImpl<>(applicationsList, PageRequest.of(0, 2), applicationsList.size());

        Page<ApplicationDTO> dtoPage = applicationsPage.map(mapper::toDto);

        Map<String, Object> response = Map.of(
                "applications", dtoPage.getContent(),
                "currentPage", dtoPage.getNumber(),
                "totalItems", dtoPage.getTotalElements(),
                "totalPages", dtoPage.getTotalPages()
        );

        when(applicationService.getApplicationsByNameAndStatus(anyString(), any(), anyInt(), anyInt())).thenReturn(applicationsPage);
        when(applicationService.getPaginatedApplicationsResponse(dtoPage)).thenReturn(response);

        mockMvc.perform(get("/applications")
                        .param("status", "CREATED")
                        .param("name", "Name")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications", hasSize(2)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(applicationService, times(1)).getApplicationsByNameAndStatus(anyString(), any(), anyInt(), anyInt());
        verify(applicationService, times(1)).getPaginatedApplicationsResponse(dtoPage);
    }
}