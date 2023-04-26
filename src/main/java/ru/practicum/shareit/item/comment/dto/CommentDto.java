package ru.practicum.shareit.item.comment.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.util.Create;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    public static final String DATE_FORMAT = "yyyy-MM-ddTHH:mm:ss";
    private Long id;

    @NotBlank(groups = {Create.class})
    private String text;

    private String authorName;

    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime created;
}
