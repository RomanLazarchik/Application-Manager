package roman.lazarchik.ApplicationManager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;

public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {
}