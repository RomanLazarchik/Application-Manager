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

    @Query("SELECT MAX(a.publishedNumber) FROM Application a")
    Optional<Integer> findMaxPublishedNumber();
}


