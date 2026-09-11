package es.upm.miw.devops.domain.services;

import es.upm.miw.devops.domain.exceptions.NotFoundException;
import es.upm.miw.devops.domain.model.User;
import es.upm.miw.devops.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(String id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id: " + id));
    }
}
