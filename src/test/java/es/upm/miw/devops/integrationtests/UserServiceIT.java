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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ── Feature 1: GET /user/{id} ──────────────────────────────────────────────

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

    // ── Feature 2: GET /user (search with filters) ────────────────────────────

    @Test
    void testFindAllReturnsAllSeededUsers() {
        // When
        List<User> result = userService.findAll();

        // Then: seeder loads 5 users
        assertThat(result).isNotNull().hasSize(5);
    }

    @Test
    void testFindByActiveTrue() {
        // When
        List<User> result = userService.findByActive(true);

        // Then: seeder has 4 active users (John, Jane, Carlos, Luis)
        assertThat(result).isNotNull().hasSize(4);
        assertThat(result).allMatch(User::isActive);
    }

    @Test
    void testFindByActiveFalse() {
        // When
        List<User> result = userService.findByActive(false);

        // Then: seeder has 1 inactive user (Ana Martínez)
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).noneMatch(User::isActive);
        assertThat(result.get(0).getFirstName()).isEqualTo("Ana");
    }

    @Test
    void testFindBillableReturnsOnlyCompleteUsers() {
        // When
        List<User> result = userService.findBillable();

        // Then: seeder has 4 billable users (Ana Martínez has null address fields)
        assertThat(result).isNotNull().hasSize(4);
        assertThat(result).allMatch(u ->
                u.getFirstName() != null && !u.getFirstName().isBlank() &&
                u.getFamilyName() != null && !u.getFamilyName().isBlank() &&
                u.getEmail() != null && !u.getEmail().isBlank() &&
                u.getIdentity() != null && !u.getIdentity().isBlank() &&
                u.getAddress() != null && !u.getAddress().isBlank() &&
                u.getCity() != null && !u.getCity().isBlank() &&
                u.getProvince() != null && !u.getProvince().isBlank() &&
                u.getPostalCode() != null && !u.getPostalCode().isBlank()
        );
    }

    @Test
    void testFindBillableExcludesIncompleteUsers() {
        // When
        List<User> result = userService.findBillable();

        // Then: Ana Martínez (incomplete address fields) must NOT appear
        assertThat(result).noneMatch(u -> "Ana".equals(u.getFirstName()));
    }

    // ── Feature 3: DELETE /user/{id} ──────────────────────────────────────────

    @Test
    void testDeleteByIdOk() {
        // Given: create a temporary user (not part of seeder) to delete
        User tempUser = userRepository.save(new User("Temp", "User", "temp@test.com",
                "99999999T", "Calle Test 1", "Madrid", "Madrid", "28000", true, Role.CUSTOMER));
        String tempId = tempUser.getId();
        assertThat(userRepository.findById(tempId)).isPresent();

        // When
        userService.deleteById(tempId);

        // Then: user no longer exists → findById throws NotFoundException
        assertThrows(NotFoundException.class, () -> userService.findById(tempId));
        assertThat(userRepository.findById(tempId)).isEmpty();
    }

    @Test
    void testDeleteByIdNotFound() {
        // Given: a non-existent id
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // When / Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteById(nonExistentId));

        assertThat(exception.getMessage()).contains(nonExistentId);
    }

    // ── Feature 4: PUT /user/{id}/active ──────────────────────────────────────

    @Test
    void testUpdateActiveToFalseOk() {
        // Given: create a temporary active user
        User tempUser = userRepository.save(new User("Active", "User", "active@test.com",
                "11111111A", "Calle Activa 1", "Madrid", "Madrid", "28001", true, Role.CUSTOMER));

        // When: set active to false
        User result = userService.updateActive(tempUser.getId(), false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tempUser.getId());
        assertThat(result.isActive()).isFalse();

        // Cleanup
        userRepository.deleteById(tempUser.getId());
    }

    @Test
    void testUpdateActiveToTrueOk() {
        // Given: create a temporary inactive user
        User tempUser = userRepository.save(new User("Inactive", "User", "inactive@test.com",
                "22222222B", "Calle Inactiva 2", "Sevilla", "Sevilla", "41001", false, Role.CUSTOMER));

        // When: set active to true
        User result = userService.updateActive(tempUser.getId(), true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tempUser.getId());
        assertThat(result.isActive()).isTrue();

        // Cleanup
        userRepository.deleteById(tempUser.getId());
    }

    @Test
    void testUpdateActiveNotFound() {
        // Given: a non-existent id
        String nonExistentId = "00000000-0000-0000-0000-111111111111";

        // When / Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateActive(nonExistentId, false));

        assertThat(exception.getMessage()).contains(nonExistentId);
    }

    // ── Feature 5: PUT /user/{id} ─────────────────────────────────────────────

    @Test
    void testUpdateOk() {
        // Given: create a temporary user
        User tempUser = userRepository.save(new User("OldName", "OldFamily", "old@test.com",
                "12345678Z", "Old Address", "Old City", "Old Province", "11111", true, Role.CUSTOMER));

        User updatedData = new User();
        updatedData.setFirstName("NewName");
        updatedData.setFamilyName("NewFamily");
        updatedData.setEmail("new@test.com");
        updatedData.setIdentity("87654321A");
        updatedData.setAddress("New Address");
        updatedData.setCity("New City");
        updatedData.setProvince("New Province");
        updatedData.setPostalCode("22222");
        updatedData.setActive(false);
        updatedData.setRole(Role.ADMIN);

        // When
        User result = userService.update(tempUser.getId(), updatedData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tempUser.getId());
        assertThat(result.getFirstName()).isEqualTo("NewName");
        assertThat(result.getFamilyName()).isEqualTo("NewFamily");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getIdentity()).isEqualTo("87654321A");
        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(result.getCity()).isEqualTo("New City");
        assertThat(result.getProvince()).isEqualTo("New Province");
        assertThat(result.getPostalCode()).isEqualTo("22222");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);

        // Cleanup
        userRepository.deleteById(tempUser.getId());
    }

    @Test
    void testUpdateNotFound() {
        // Given: a non-existent id
        String nonExistentId = "00000000-0000-0000-0000-222222222222";
        User updatedData = new User();
        updatedData.setFirstName("AnyName");

        // When / Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(nonExistentId, updatedData));

        assertThat(exception.getMessage()).contains(nonExistentId);
    }
}

