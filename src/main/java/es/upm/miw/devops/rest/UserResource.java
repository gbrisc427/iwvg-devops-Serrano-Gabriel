package es.upm.miw.devops.rest;

import es.upm.miw.devops.domain.services.UserService;
import es.upm.miw.devops.rest.dtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "User management API")
@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {

    public static final String USERS = "/user";
    public static final String ID = "/{id}";


    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Find user by id", description = "Returns the user with the given id")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDto findById(@PathVariable String id) {
        return new UserDto(this.userService.findById(id));
    }
}
