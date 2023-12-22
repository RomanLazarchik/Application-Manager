package roman.lazarchik.ApplicationManager.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;

import java.util.Optional;


public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Page<Application> findByNameContainingAndStatus(String name, ApplicationStatus status, Pageable pageable);

    Page<Application> findByNameContaining(String name, Pageable pageable);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    @Query("SELECT MAX(a.publishedNumber) FROM Application a WHERE a.status = 'PUBLISHED'")
    Optional<Integer> findMaxPublishedNumber();

    Application findByNameAndContent(String name, String content);

    boolean existsByNameAndContent(String name, String content);

}


