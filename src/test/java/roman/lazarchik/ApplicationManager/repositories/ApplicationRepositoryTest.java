package roman.lazarchik.ApplicationManager.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import roman.lazarchik.ApplicationManager.models.Application;
import roman.lazarchik.ApplicationManager.models.ApplicationStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    public void whenFindByNameContainingAndStatusThenReturnApplications() {

        Application app1 = new Application();
        app1.setName("Name 1");
        app1.setStatus(ApplicationStatus.CREATED);
        entityManager.persist(app1);

        Application app2 = new Application();
        app2.setName("Name 2");
        app2.setStatus(ApplicationStatus.VERIFIED);
        entityManager.persist(app2);

        entityManager.flush();
        entityManager.clear();

        Page<Application> result = applicationRepository.findByNameContainingAndStatus(
                "Name", ApplicationStatus.CREATED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Name 1");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ApplicationStatus.CREATED);

    }

    @Test
    public void whenFindByNameContainingThenReturnApplications() {

        Application app1 = new Application();
        app1.setName("Name 1");
        app1.setStatus(ApplicationStatus.CREATED);
        entityManager.persist(app1);

        Application app2 = new Application();
        app2.setName("Name 2");
        app2.setStatus(ApplicationStatus.VERIFIED);
        entityManager.persist(app2);

        entityManager.flush();
        entityManager.clear();

        Page<Application> result = applicationRepository.findByNameContaining(
                "Name", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Name 1");
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(ApplicationStatus.VERIFIED);
    }

    @Test
    public void whenFindByStatusThenReturnApplications() {

        Application app1 = new Application();
        app1.setName("Name 1");
        app1.setStatus(ApplicationStatus.CREATED);
        entityManager.persist(app1);

        Application app2 = new Application();
        app2.setName("Name 2");
        app2.setStatus(ApplicationStatus.CREATED);
        entityManager.persist(app2);

        entityManager.flush();
        entityManager.clear();

        Page<Application> result = applicationRepository.findByStatus(
                ApplicationStatus.CREATED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Name 1");
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(ApplicationStatus.CREATED);
    }

    @Test
    public void whenFindByNameAndContentThenReturnApplication() {

        Application app = new Application();
        app.setId(null);
        app.setName("Name");
        app.setContent("Content");
        app.setStatus(ApplicationStatus.CREATED);
        app.setReason(null);
        app.setHistories(null);
        app.setPublishedNumber(0);
        entityManager.persist(app);

        entityManager.flush();
        entityManager.clear();

        Application found = applicationRepository.findByNameAndContent("Name", "Content");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Name");
        assertThat(found.getContent()).isEqualTo("Content");
    }

    @Test
    void whenFindMaxPublishedNumberThenReturnMaxNumber() {

        Application app1 = new Application();
        app1.setPublishedNumber(2);
        app1.setStatus(ApplicationStatus.PUBLISHED);
        entityManager.persist(app1);

        Application app2 = new Application();
        app2.setPublishedNumber(3);
        app2.setStatus(ApplicationStatus.PUBLISHED);
        entityManager.persist(app2);

        entityManager.flush();
        entityManager.clear();

        Optional<Integer> maxPublishedNumber = applicationRepository.findMaxPublishedNumber();

        assertThat(maxPublishedNumber.isPresent()).isTrue();
        assertThat(maxPublishedNumber.get()).isEqualTo(3);
    }

    @Test
    public void whenNoPublishedApplicationsThenReturnEmpty() {

        Optional<Integer> maxPublishedNumber = applicationRepository.findMaxPublishedNumber();

        assertThat(maxPublishedNumber).isNotPresent();
    }
}