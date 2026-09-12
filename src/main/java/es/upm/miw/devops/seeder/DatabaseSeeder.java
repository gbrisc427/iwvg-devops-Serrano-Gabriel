package es.upm.miw.devops.seeder;

import es.upm.miw.devops.domain.model.Role;
import es.upm.miw.devops.domain.model.User;
import es.upm.miw.devops.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public DatabaseSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (this.userRepository.count() == 0) {
            this.seedUsers();
        }
    }

    private void seedUsers() {
        List<User> users = List.of(
                new User("John", "Doe", "john.doe@example.com", "12345678A",
                        "Calle Mayor 1", "Madrid", "Madrid", "28001", true, Role.ADMIN),
                new User("Jane", "Smith", "jane.smith@example.com", "87654321B",
                        "Avenida Paz 5", "Barcelona", "Barcelona", "08001", true, Role.CUSTOMER),
                new User("Carlos", "García", "carlos.garcia@example.com", "11223344C",
                        "Calle Luna 10", "Sevilla", "Sevilla", "41001", true, Role.CUSTOMER),
                new User("Ana", "Martínez", "ana.martinez@example.com", "44332211D",
                        null, null, null, null, false, Role.CUSTOMER),
                new User("Luis", "López", "luis.lopez@example.com", "55667788E",
                        "Gran Vía 22", "Valencia", "Valencia", "46001", true, Role.CUSTOMER)
        );
        this.userRepository.saveAll(users);
    }
}
