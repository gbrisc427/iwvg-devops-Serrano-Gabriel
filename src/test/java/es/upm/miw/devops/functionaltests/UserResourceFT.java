package es.upm.miw.devops.functionaltests;

import es.upm.miw.devops.domain.model.Role;
import es.upm.miw.devops.domain.model.User;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserResourceFT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    // ── Feature 1: GET /user/{id} ──────────────────────────────────────────────

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

    // ── Feature 2: GET /user (search with filters) ────────────────────────────

    @Test
    void testFindAllReturnsAllSeededUsers() {
        // When / Then: 5 users seeded
        webTestClient.get()
                .uri(UserResource.USERS)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .value((List<UserDto> list) -> assertThat(list).hasSize(5));
    }

    @Test
    void testFindByActiveTrueReturnsOnlyActiveUsers() {
        // When / Then: 4 active users seeded
        webTestClient.get()
                .uri(UserResource.USERS + "?active=true")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .value((List<UserDto> list) -> {
                    assertThat(list).hasSize(4);
                    assertThat(list).allMatch(UserDto::isActive);
                });
    }

    @Test
    void testFindByActiveFalseReturnsOnlyInactiveUsers() {
        // When / Then: 1 inactive user seeded (Ana Martínez)
        webTestClient.get()
                .uri(UserResource.USERS + "?active=false")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .value((List<UserDto> list) -> {
                    assertThat(list).hasSize(1);
                    assertThat(list).noneMatch(UserDto::isActive);
                    assertThat(list.get(0).getFirstName()).isEqualTo("Ana");
                });
    }

    @Test
    void testFindBillableReturnsBillableUsers() {
        // When / Then: 4 billable users (Ana has null address fields)
        webTestClient.get()
                .uri(UserResource.USERS + "?billable=true")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .value((List<UserDto> list) -> {
                    assertThat(list).hasSize(4);
                    assertThat(list).allMatch(dto ->
                            dto.getFirstName() != null && !dto.getFirstName().isBlank() &&
                            dto.getFamilyName() != null && !dto.getFamilyName().isBlank() &&
                            dto.getEmail() != null && !dto.getEmail().isBlank() &&
                            dto.getAddress() != null && !dto.getAddress().isBlank()
                    );
                });
    }

    @Test
    void testFindBillableExcludesIncompleteUsers() {
        // When / Then: Ana Martínez must NOT be in the billable list
        webTestClient.get()
                .uri(UserResource.USERS + "?billable=true")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .value((List<UserDto> list) ->
                        assertThat(list).noneMatch(dto -> "Ana".equals(dto.getFirstName()))
                );
    }

    // ── Feature 3: DELETE /user/{id} ──────────────────────────────────────────

    @Test
    void testDeleteByIdOk() {
        // Given: save a temporary user to delete (keeps seeded data intact)
        String tempId = userRepository.save(
                new User("Temp", "Delete", "temp.delete@test.com",
                        "88888888T", "Calle Borrar 1", "Madrid", "Madrid", "28000",
                        true, Role.CUSTOMER)
        ).getId();


        // When: DELETE the user
        webTestClient.delete()
                .uri(UserResource.USERS + "/" + tempId)
                .exchange()
                .expectStatus().isNoContent();

        // Then: a follow-up GET confirms the user is gone
        webTestClient.get()
                .uri(UserResource.USERS + "/" + tempId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteByIdNotFound() {
        // Given: a non-existent id
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // When / Then
        webTestClient.delete()
                .uri(UserResource.USERS + "/" + nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── Feature 4: PUT /user/{id}/active ──────────────────────────────────────

    @Test
    void testUpdateActiveToFalseOk() {
        // Given: create a temporary active user
        String tempId = userRepository.save(
                new User("Active", "Temp", "active.temp@test.com",
                        "33333333C", "Calle Activa 3", "Madrid", "Madrid", "28002",
                        true, Role.CUSTOMER)
        ).getId();

        // When: PUT active = false
        webTestClient.put()
                .uri(UserResource.USERS + "/" + tempId + "/active")
                .bodyValue(false)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(dto -> {
                    assertThat(dto.getId()).isEqualTo(tempId);
                    assertThat(dto.isActive()).isFalse();
                });

        // Cleanup
        userRepository.deleteById(tempId);
    }

    @Test
    void testUpdateActiveToTrueOk() {
        // Given: create a temporary inactive user
        String tempId = userRepository.save(
                new User("Inactive", "Temp", "inactive.temp@test.com",
                        "44444444D", "Calle Inactiva 4", "Valencia", "Valencia", "46002",
                        false, Role.CUSTOMER)
        ).getId();

        // When: PUT active = true
        webTestClient.put()
                .uri(UserResource.USERS + "/" + tempId + "/active")
                .bodyValue(true)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .value(dto -> {
                    assertThat(dto.getId()).isEqualTo(tempId);
                    assertThat(dto.isActive()).isTrue();
                });

        // Cleanup
        userRepository.deleteById(tempId);
    }

    @Test
    void testUpdateActiveNotFound() {
        // Given: a non-existent id
        String nonExistentId = "00000000-0000-0000-0000-111111111111";

        // When / Then
        webTestClient.put()
                .uri(UserResource.USERS + "/" + nonExistentId + "/active")
                .bodyValue(false)
                .exchange()
                .expectStatus().isNotFound();
    }
}
