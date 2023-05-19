package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    Long id;

    @NotBlank(groups = {Create.class}, message = "Name cannot be empty")
    @Size(groups = {Create.class, Update.class}, min = 1, message = "Name cannot be empty")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Email cannot be empty")
    @Email(groups = {Update.class, Create.class}, message = "Email is not correct")
    @Size(groups = {Create.class, Update.class}, min = 1, message = "Email is not correct")
    private String email;
}
