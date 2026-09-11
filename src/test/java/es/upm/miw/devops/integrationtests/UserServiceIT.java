package es.upm.miw.devops.integrationtests;

import es.upm.miw.devops.domain.exceptions.NotFoundException;
import es.upm.miw.devops.domain.model.Role;
import es.upm.miw.devops.domain.model.User;
import es.upm.miw.devops.domain.services.UserService;
import es.upm.miw.devops.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByIdOk() {
        // Given: a user seeded by DatabaseSeeder (we fetch any existing one)
        User seededUser = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeder did not load any users"));

        // When
        User result = userService.findById(seededUser.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(seededUser.getId());
        assertThat(result.getFirstName()).isNotBlank();
        assertThat(result.getFamilyName()).isNotBlank();
        assertThat(result.getRole()).isNotNull();
    }

    @Test
    void testFindByIdAdminUser() {
        // Given: the ADMIN user seeded by DatabaseSeeder
        User adminUser = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeder did not load an ADMIN user"));

        // When
        User result = userService.findById(adminUser.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void testFindByIdNotFound() {
        // Given: a random id that does not exist in the database
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // When / Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findById(nonExistentId));

        assertThat(exception.getMessage()).contains(nonExistentId);
    }
}
