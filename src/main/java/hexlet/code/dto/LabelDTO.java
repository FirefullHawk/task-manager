package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class LabelDTO {
    private long id;

    @NonNull
    @NotBlank(message = "Field 'name' must not be empty!")
    private String name;

    private Date createdAt;
}
