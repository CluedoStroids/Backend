package at.aau.se2.cluedo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class CluedoApplicationTest {

    @MockBean
    private SpringApplication springApplication;

    @Test
    void contextLoads() {
        // Verify the Spring context loads successfully
        assertDoesNotThrow(() -> {
            CluedoApplication.main(new String[]{});
        });
    }
}
