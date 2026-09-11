package es.upm.miw.devops.functionaltests;

import es.upm.miw.devops.persistence.UserRepository;
import es.upm.miw.devops.rest.UserResource;
import es.upm.miw.devops.rest.dtos.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserResourceFT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByIdOk() {
        // Given: fetch a seeded user id at runtime
        String seededId = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeder did not load any users"))
                .getId();

        // When / Then
        webTestClient.get()
                .uri(UserResource.USERS + "/" + seededId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(seededId);
                    assertThat(dto.getFirstName()).isNotBlank();
                    assertThat(dto.getFamilyName()).isNotBlank();
                    assertThat(dto.getRole()).isNotNull();
                });
    }

    @Test
    void testFindByIdReturnsAllFields() {
        // Given: the ADMIN user seeded (John Doe)
        String adminId = userRepository.findAll().stream()
                .filter(u -> "John".equals(u.getFirstName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeder did not load John Doe"))
                .getId();

        // When / Then
        webTestClient.get()
                .uri(UserResource.USERS + "/" + adminId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(dto -> {
                    assertThat(dto.getFirstName()).isEqualTo("John");
                    assertThat(dto.getFamilyName()).isEqualTo("Doe");
                    assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
                    assertThat(dto.isActive()).isTrue();
                });
    }

    @Test
    void testFindByIdNotFound() {
        // Given: a non-existent id
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // When / Then
        webTestClient.get()
                .uri(UserResource.USERS + "/" + nonExistentId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }
}
