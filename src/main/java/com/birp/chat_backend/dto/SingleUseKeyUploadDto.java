package com.birp.chat_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class SingleUseKeyUploadDto {
    private int userId;

    @NotEmpty
    private List<String> publicKeys;
}
