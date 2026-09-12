package es.upm.miw.devops.rest;

import es.upm.miw.devops.domain.services.UserService;
import es.upm.miw.devops.rest.dtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(
            summary = "Search users",
            description = "Returns users. Optional filters: active (true/false) or billable (true). " +
                    "If no filter is provided, all users are returned."
    )
    @ApiResponse(responseCode = "200", description = "List of users")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean billable) {

        if (Boolean.TRUE.equals(billable)) {
            return this.userService.findBillable().stream()
                    .map(UserDto::new).toList();
        }
        if (active != null) {
            return this.userService.findByActive(active).stream()
                    .map(UserDto::new).toList();
        }
        return this.userService.findAll().stream()
                .map(UserDto::new).toList();
    }

    @Operation(summary = "Find user by id", description = "Returns the user with the given id")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDto findById(@PathVariable String id) {
        return new UserDto(this.userService.findById(id));
    }

    @Operation(summary = "Delete user by id", description = "Deletes the user with the given id")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable String id) {
        this.userService.deleteById(id);
    }
}


