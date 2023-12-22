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

//    @Test
//    public void testFindByNameContainingAndStatus() {
//
//        Application app1 = new Application();
//        app1.setName("Test Application 1");
//        app1.setStatus(ApplicationStatus.CREATED);
//        entityManager.persist(app1);
//
//        Application app2 = new Application();
//        app2.setName("Another Application");
//        app2.setStatus(ApplicationStatus.VERIFIED);
//        entityManager.persist(app2);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        Page<Application> result = applicationRepository.findByNameContainingAndStatus(
//                "Test", ApplicationStatus.CREATED, PageRequest.of(0, 10));
//
//        assertThat(result).hasSize(1);
//        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Application 1");
//        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ApplicationStatus.CREATED);
//    }

    @Test
    void testFindMaxPublishedNumber() {

        Application app1 = new Application();
        app1.setPublishedNumber(1);
        app1.setStatus(ApplicationStatus.PUBLISHED);
        entityManager.persist(app1);

        Application app2 = new Application();
        app2.setPublishedNumber(2);
        app2.setStatus(ApplicationStatus.PUBLISHED);
        entityManager.persist(app2);

        Application app3 = new Application();
        app3.setPublishedNumber(3);
        app3.setStatus(ApplicationStatus.CREATED);
        entityManager.persist(app3);

        entityManager.flush();
        entityManager.clear();

        Optional<Integer> maxPublishedNumber = applicationRepository.findMaxPublishedNumber();

        assertThat(maxPublishedNumber.isPresent()).isTrue();
        assertThat(maxPublishedNumber.get()).isEqualTo(2);
    }

}