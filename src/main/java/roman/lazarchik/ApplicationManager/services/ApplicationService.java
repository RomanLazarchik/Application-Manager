package roman.lazarchik.ApplicationManager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roman.lazarchik.ApplicationManager.dto.RejectDeleteDTO;
import roman.lazarchik.ApplicationManager.exceptions.ApplicationNotFoundException;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.repositories.ApplicationRepository;

import java.time.LocalDateTime;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository repository;

    @Autowired
    private ApplicationHistoryService historyService;


    private void saveHistory(Application app, ApplicationStatus newStatus) {
        ApplicationHistory history = new ApplicationHistory();
        history.setTimestamp(LocalDateTime.now());
        history.setStatus(newStatus);
        history.setApplication(app);
        historyService.saveHistory(history);
    }

    public Application createApplication(Application app) {
        app.setStatus(ApplicationStatus.CREATED);
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.CREATED);
        return savedApp;
    }

    @Transactional
    public Application updateContent(Long id, String content) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() == ApplicationStatus.CREATED || app.getStatus() == ApplicationStatus.VERIFIED) {
            app.setContent(content);
            Application savedApp = repository.save(app);
            saveHistory(savedApp, savedApp.getStatus());  // просто сохраняем историю с текущим статусом
            return savedApp;
        } else {
            throw new RuntimeException("Cannot edit content in this status");
        }
    }


    @Transactional
    public Application rejectApplication(Long id, RejectDeleteDTO rejectDTO) {
        Application app = repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        // Проверка текущего статуса
        if (app.getStatus() != ApplicationStatus.VERIFIED && app.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new RuntimeException("Can only reject applications with status VERIFIED or ACCEPTED");
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

        if (app.getStatus() != ApplicationStatus.CREATED) {
            throw new RuntimeException("Application can only be deleted in the CREATED status.");
        }

        app.setStatus(ApplicationStatus.DELETED);
        app.setReason(deleteDTO.getReason());
        repository.save(app);
        saveHistory(app, ApplicationStatus.DELETED); // сохраняем историю изменения статуса
    }

    @Transactional
    public Application verifyApplication(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() != ApplicationStatus.CREATED) {
            throw new RuntimeException("Application can only be verified in the CREATED status.");
        }

        app.setStatus(ApplicationStatus.VERIFIED);
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.VERIFIED); // сохраняем историю изменения статуса
        return savedApp;
    }

    @Transactional
    public Application acceptApplication(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (app.getStatus() != ApplicationStatus.VERIFIED) {
            throw new RuntimeException("Application can only be accepted in the VERIFIED status.");
        }

        app.setStatus(ApplicationStatus.ACCEPTED);
        Application savedApp = repository.save(app);
        saveHistory(savedApp, ApplicationStatus.ACCEPTED); // сохраняем историю изменения статуса
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
            throw new RuntimeException("Application is already published");
        }
    }
}


