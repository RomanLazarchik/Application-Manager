package roman.lazarchik.ApplicationManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roman.lazarchik.ApplicationManager.models.ApplicationHistory;
import roman.lazarchik.ApplicationManager.repositories.ApplicationHistoryRepository;

@Service
@RequiredArgsConstructor
public class ApplicationHistoryService {

    private final ApplicationHistoryRepository repository;

    public void saveHistory(ApplicationHistory history) {
        repository.save(history);
    }
}