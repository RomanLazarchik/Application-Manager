package roman.lazarchik.ApplicationManager.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roman.lazarchik.ApplicationManager.dto.RejectDeleteDTO;
import roman.lazarchik.ApplicationManager.dto.UpdateContentDTO;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.services.ApplicationService;

import java.util.Optional;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application app) {
        Application createdApp = service.createApplication(app);
        return new ResponseEntity<>(createdApp, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Application> updateContent(@PathVariable Long id, @RequestBody UpdateContentDTO updateContentDTO) {
        Application updatedApp = service.updateContent(id, updateContentDTO.getContent());
        return new ResponseEntity<>(updatedApp, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id, @RequestBody RejectDeleteDTO deleteDTO) {
        service.deleteApplication(id, deleteDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<Application> verifyApplication(@PathVariable Long id) {
        Application verifiedApp = service.verifyApplication(id);
        return new ResponseEntity<>(verifiedApp, HttpStatus.OK);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Application> rejectApplication(@PathVariable Long id, @RequestBody RejectDeleteDTO rejectDTO) {
        Application updatedApp = service.rejectApplication(id, rejectDTO);
        return new ResponseEntity<>(updatedApp, HttpStatus.OK);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Application> acceptApplication(@PathVariable Long id) {
        Application acceptedApp = service.acceptApplication(id);
        return new ResponseEntity<>(acceptedApp, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<Application>> getApplications(
            @RequestParam Optional<String> name,
            @RequestParam Optional<ApplicationStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Application> applications = service.getApplications(name.orElse(""), status.orElse(null), page, size);
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Application> publishApplication(@PathVariable Long id) {
        Application publishedApp = service.publishApplication(id);
        return new ResponseEntity<>(publishedApp, HttpStatus.OK);
    }
}


