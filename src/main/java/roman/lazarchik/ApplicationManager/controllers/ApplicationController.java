package roman.lazarchik.ApplicationManager.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roman.lazarchik.ApplicationManager.dto.ApplicationDTO;
import roman.lazarchik.ApplicationManager.dto.DeleteDTO;
import roman.lazarchik.ApplicationManager.dto.RejectDTO;
import roman.lazarchik.ApplicationManager.dto.UpdateContentDTO;
import roman.lazarchik.ApplicationManager.mapper.ApplicationMapper;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;
import roman.lazarchik.ApplicationManager.services.ApplicationService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("${application.endpoint.root}")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;
    private final ApplicationMapper mapper;

    @PostMapping
    public ResponseEntity<ApplicationDTO> createApplication(@RequestBody ApplicationDTO appDTO) {
        Application createdApp = service.createApplication(mapper.toEntity(appDTO));
        return new ResponseEntity<>(mapper.toDto(createdApp), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDTO> updateContent(@PathVariable Long id, @Valid @RequestBody UpdateContentDTO updateContent) {
        Application updatedApp = service.updateContent(id, updateContent.getContent());
        return new ResponseEntity<>(mapper.toDto(updatedApp), HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id, @Valid @RequestBody DeleteDTO reasonDelete) {
        service.deleteApplication(id, reasonDelete);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<ApplicationDTO> verifyApplication(@PathVariable Long id) {
        Application verifiedApp = service.verifyApplication(id);
        return new ResponseEntity<>(mapper.toDto(verifiedApp), HttpStatus.OK);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApplicationDTO> rejectApplication(@PathVariable Long id, @Valid @RequestBody RejectDTO reasonReject) {
        Application rejectApp = service.rejectApplication(id, reasonReject);
        return new ResponseEntity<>(mapper.toDto(rejectApp), HttpStatus.OK);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApplicationDTO> acceptApplication(@PathVariable Long id) {
        Application acceptedApp = service.acceptApplication(id);
        return new ResponseEntity<>(mapper.toDto(acceptedApp), HttpStatus.OK);
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApplicationDTO> publishApplication(@PathVariable Long id) {
        Application publishedApp = service.publishApplication(id);
        return new ResponseEntity<>(mapper.toDto(publishedApp), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApplications(
            @RequestParam Optional<String> name,
            @RequestParam Optional<ApplicationStatus> status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Page<Application> applications;
        if (name.isPresent() && status.isPresent()) {
            applications = service.getApplicationsByNameAndStatus(name.get(), status.get(), page, size);
        } else if (name.isPresent()) {
            applications = service.getApplicationsByName(name.get(), page, size);
        } else if (status.isPresent()) {
            applications = service.getApplicationsByStatus(status.get(), page, size);
        } else {
            applications = service.getAllApplications(page, size);
        }

        Page<ApplicationDTO> dtoPage = applications.map(mapper::toDto);

        Map<String, Object> response = service.getPaginatedApplicationsResponse(dtoPage);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}


