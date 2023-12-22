package roman.lazarchik.ApplicationManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roman.lazarchik.ApplicationManager.dto.ApplicationDTO;
import roman.lazarchik.ApplicationManager.dto.DeleteDTO;
import roman.lazarchik.ApplicationManager.dto.RejectDTO;
import roman.lazarchik.ApplicationManager.exceptions.*;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.repositories.ApplicationRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationHistoryService historyService;

    private void saveHistory(Application app, ApplicationStatus newStatus, boolean contentUpdated) {
        ApplicationHistory history = new ApplicationHistory();
        history.setTimestamp(LocalDateTime.now());
        history.setStatus(newStatus);
        history.setApplication(app);
        history.setContentUpdated(contentUpdated);
        historyService.saveHistory(history);
    }

    @Transactional
    public Application createApplication(Application app) {

        if (app == null || app.getName() == null || app.getName().trim().isEmpty()
                || app.getContent() == null || app.getContent().trim().isEmpty()) {
            throw new InvalidInputException("Fields 'name' and 'content' must not be null or empty");
        }

//        if (repository.existsByNameAndContent(app.getName(), app.getContent())) {
//            throw new ApplicationAlreadyExistsException("Application with name '" + app.getName() + "' and content already exists");
//        }

        Application existingApp = repository.findByNameAndContent(app.getName(), app.getContent());
        if (existingApp != null) {
            return existingApp;
        }

        app.setStatus(ApplicationStatus.CREATED);
        repository.save(app);
        saveHistory(app, ApplicationStatus.CREATED, false);
        return app;
    }

    @Transactional
    public Application updateContent(Long id, String updateContent) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() != ApplicationStatus.CREATED && app.getStatus() != ApplicationStatus.VERIFIED) {
            throw new ContentEditNotAllowedException("Cannot edit content in this status. Content can only be edited in CREATED or VERIFIED status.");
        }

        if (!app.getContent().equals(updateContent)) {
            app.setContent(updateContent);
            Application savedApp = repository.save(app);
            saveHistory(savedApp, savedApp.getStatus(), true);
            return savedApp;
        } else {
            return app;
        }
    }

    @Transactional
    public Application rejectApplication(Long id, RejectDTO reasonReject) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.REJECTED && app.getReason().equals(reasonReject.getReason())) {
            return app;
        }

        if (app.getStatus() != ApplicationStatus.VERIFIED && app.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidApplicationStatusException("Can only reject applications with status VERIFIED or ACCEPTED");
        }
        if (reasonReject.getReason() == null || reasonReject.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided for rejecting an application. Please provide a valid reason in the 'reason' field.");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setReason(reasonReject.getReason());
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.REJECTED, false);
        return savedApp;
    }

    @Transactional
    public void deleteApplication(Long id, DeleteDTO reasonDelete) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.DELETED) {
            return;
        }

        if (app.getStatus() != ApplicationStatus.CREATED) {
            throw new InvalidApplicationStatusException("Application can only be deleted in the CREATED status.");
        }

        if (reasonDelete.getReason() == null || reasonDelete.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided for deleting an application. Please provide a valid reason in the 'reason' field.");
        }

        app.setStatus(ApplicationStatus.DELETED);
        app.setReason(reasonDelete.getReason());
        repository.save(app);
        saveHistory(app, ApplicationStatus.DELETED, false);
    }

    @Transactional
    public Application verifyApplication(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.VERIFIED) {
            return app;
        }

        if (app.getStatus() != ApplicationStatus.CREATED) {
            throw new InvalidApplicationStatusException("Application can only be verified in the CREATED status.");
        }

        app.setStatus(ApplicationStatus.VERIFIED);
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.VERIFIED, false);
        return savedApp;
    }

    @Transactional
    public Application acceptApplication(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.ACCEPTED) {
            return app;
        }

        if (app.getStatus() != ApplicationStatus.VERIFIED) {
            throw new InvalidApplicationStatusException("Application can only be accepted in the VERIFIED status.");
        }

        app.setStatus(ApplicationStatus.ACCEPTED);
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.ACCEPTED, false);
        return savedApp;
    }

    @Transactional
    public Application publishApplication(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.PUBLISHED) {
            return app;
        }

        if (app.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidApplicationStatusException("Application can only be published in the ACCEPTED status");
        } else {
            Integer currentMaxNumber = repository.findMaxPublishedNumber().orElse(0);
            app.setPublishedNumber(currentMaxNumber + 1);
            app.setStatus(ApplicationStatus.PUBLISHED);
            repository.save(app);
            saveHistory(app, ApplicationStatus.PUBLISHED, false);
            return app;
        }
    }

    public Page<Application> getApplicationsByNameAndStatus(String name, ApplicationStatus status, int page, int size) {
        try {
            if (name.trim().isEmpty()) {
                throw new InvalidInputException("Name parameter must not be empty");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Application> applications = repository.findByNameContainingAndStatus(name, status, pageable);

            if (applications.isEmpty()) {
                throw new ApplicationNotFoundException("No applications found with the provided name and status");
            }

            return applications;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error occurred while accessing the database", e);
        }
    }

    public Page<Application> getApplicationsByName(String name, int page, int size) {
        try {
            if (name.trim().isEmpty()) {
                throw new InvalidInputException("Name parameter must not empty");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Application> applications = repository.findByNameContaining(name, pageable);

            if (applications.isEmpty()) {
                throw new ApplicationNotFoundException("No applications found with the provided name");
            }

            return applications;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error occurred while accessing the database", e);
        }
    }

    public Page<Application> getApplicationsByStatus(ApplicationStatus status, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Application> applications = repository.findByStatus(status, pageable);

            if (applications.isEmpty()) {
                throw new ApplicationNotFoundException("No applications found with the provided name");
            }

            return applications;
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error occurred while accessing the database", e);
        }
    }

    public Page<Application> getAllApplications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Map<String, Object> getPaginatedApplicationsResponse(Page<ApplicationDTO> applications) {


        Map<String, Object> response = new HashMap<>();
        response.put("applications", applications.getContent());
        response.put("currentPage", applications.getNumber());
        response.put("totalItems", applications.getTotalElements());
        response.put("totalPages", applications.getTotalPages());

        return response;
    }
}


