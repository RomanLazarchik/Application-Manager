package roman.lazarchik.ApplicationManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roman.lazarchik.ApplicationManager.dto.RejectDeleteDTO;
import roman.lazarchik.ApplicationManager.exceptions.*;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.repositories.ApplicationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationHistoryService historyService;

    private void saveHistory(Application app, ApplicationStatus newStatus) {
        ApplicationHistory history = new ApplicationHistory();
        history.setTimestamp(LocalDateTime.now());
        history.setStatus(newStatus);
        history.setApplication(app);
        historyService.saveHistory(history);
    }
    @Transactional
    public Application createApplication(Application app) {
        if (app == null || app.getName() == null || app.getName().trim().isEmpty()
                || app.getContent() == null || app.getContent().trim().isEmpty()) {
            throw new InvalidInputException("Fields 'name' and 'content' must not be null or empty");
        }

        app.setStatus(ApplicationStatus.CREATED);
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.CREATED);
        return savedApp;
    }
    @Transactional
    public Application updateContent(Long id, String content) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() != ApplicationStatus.CREATED && app.getStatus() != ApplicationStatus.VERIFIED) {
            throw new ContentEditNotAllowedException("Cannot edit content in this status");
        }

        if (!app.getContent().equals(content)) {
            app.setContent(content);
            Application savedApp = repository.save(app);
            saveHistory(savedApp, savedApp.getStatus());
            return savedApp;
        } else {
            return app;
        }
    }
    @Transactional
    public Application rejectApplication(Long id, RejectDeleteDTO rejectDTO) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.REJECTED && app.getReason().equals(rejectDTO.getReason())) {
            return app;
        }

        if (app.getStatus() != ApplicationStatus.VERIFIED && app.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidApplicationStatusException("Can only reject applications with status VERIFIED or ACCEPTED");
        }
        if (rejectDTO.getReason() == null || rejectDTO.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided for rejecting an application. Please provide a valid reason in the 'reason' field.");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setReason(rejectDTO.getReason());
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.REJECTED);
        return savedApp;
    }
    @Transactional
    public void deleteApplication(Long id, RejectDeleteDTO deleteDTO) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.DELETED) {
            return;
        }

        if (app.getStatus() != ApplicationStatus.CREATED) {
            throw new InvalidApplicationStatusException("Application can only be deleted in the CREATED status.");
        }

        if (deleteDTO.getReason() == null || deleteDTO.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided for deleting an application. Please provide a valid reason in the 'reason' field.");
        }

        app.setStatus(ApplicationStatus.DELETED);
        app.setReason(deleteDTO.getReason());
        repository.save(app);
        saveHistory(app, ApplicationStatus.DELETED);
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
        saveHistory(savedApp, ApplicationStatus.VERIFIED);
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
        saveHistory(savedApp, ApplicationStatus.ACCEPTED);
        return savedApp;
    }

    public Page<Application> getApplications(String name, ApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByNameContainingAndStatus(name, status, pageable);
    }
    @Transactional
    public Application publishApplication(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() != ApplicationStatus.PUBLISHED) {
            Integer currentMaxNumber = repository.findMaxPublishedNumber().orElse(0);
            app.setPublishedNumber(currentMaxNumber + 1);
            app.setStatus(ApplicationStatus.PUBLISHED);
            repository.save(app);
            saveHistory(app, ApplicationStatus.PUBLISHED);
            return app;
        } else {
            throw new ApplicationAlreadyPublishedException("Application is already published");
        }
    }
}


