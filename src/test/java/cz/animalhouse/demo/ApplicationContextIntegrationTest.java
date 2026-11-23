package cz.animalhouse.demo;

import cz.animalhouse.config.BreedingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationContextIntegrationTest {

    @Autowired
    private BreedingProperties breedingProperties;

	@Test
	void shouldLoadApplicationContext() {
	}

    @Test
    void shouldLoadBreedingProperties() {
        assertThat(breedingProperties.getFemaleMinAgeWeeks()).isEqualTo(4);
        assertThat(breedingProperties.getMaleMinAgeWeeks()).isEqualTo(5);
    }
}
