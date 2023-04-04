package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    
    private Long id;
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email is not correct")
    private String email;
}
