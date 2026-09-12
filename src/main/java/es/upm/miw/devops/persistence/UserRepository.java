package es.upm.miw.devops.persistence;

import es.upm.miw.devops.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    List<User> findByActive(boolean active);

    @Query("SELECT u FROM User u WHERE " +
            "u.firstName IS NOT NULL AND u.firstName <> '' AND " +
            "u.familyName IS NOT NULL AND u.familyName <> '' AND " +
            "u.email IS NOT NULL AND u.email <> '' AND " +
            "u.identity IS NOT NULL AND u.identity <> '' AND " +
            "u.address IS NOT NULL AND u.address <> '' AND " +
            "u.city IS NOT NULL AND u.city <> '' AND " +
            "u.province IS NOT NULL AND u.province <> '' AND " +
            "u.postalCode IS NOT NULL AND u.postalCode <> ''")
    List<User> findBillableUsers();
}

