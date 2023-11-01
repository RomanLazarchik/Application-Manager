package roman.lazarchik.ApplicationManager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.repositories.ApplicationHistoryRepository;

@Service
public class ApplicationHistoryService {

    private final ApplicationHistoryRepository repository;

    public ApplicationHistoryService(ApplicationHistoryRepository repository) {
        this.repository = repository;
    }

    public ApplicationHistory saveHistory(ApplicationHistory history) {
        return repository.save(history);
    }
}

