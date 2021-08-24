package me.kesx.tool.entity;

import lombok.Data;
import javax.validation.constraints.NotBlank;


@Data
public class WordVo {
    private Integer wordId;
    @NotBlank(message = "word cannot be blank")
    private String wordItem;
    @NotBlank(message = "pos cannot be blank")
    private String pos;
    private String notes;
    private Integer hasMarked = 0;
    private Integer stillTough;
    private Integer round;
    private String needRememberDate;
}
