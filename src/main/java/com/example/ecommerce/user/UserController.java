package com.example.ecommerce.user;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.ResponseFactory;
import com.example.ecommerce.user.dto.UserRequestDto;
import com.example.ecommerce.user.dto.UserResponseDto;
import com.example.ecommerce.user.dto.UserUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Register user")
    ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody UserRequestDto request) {
        return ResponseFactory.created("User registered successfully", userService.register(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getEntity(#id).email")
    @Operation(summary = "Get user")
    ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long id) {
        return ResponseFactory.ok("User fetched successfully", userService.getUser(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getEntity(#id).email")
    @Operation(summary = "Update user")
    ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id,
                                                            @Valid @RequestBody UserUpdateDto request) {
        return ResponseFactory.ok("User updated successfully", userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getEntity(#id).email")
    @Operation(summary = "Delete user")
    ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseFactory.ok("User deleted successfully", null);
    }
}
