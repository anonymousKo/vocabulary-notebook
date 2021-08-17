package me.kesx.tool.entity;

import lombok.Data;


@Data
public class WordVo {
    private Integer wordId;
    private String word;
    private String pos;
    private String notes;
    private Integer hasMarked;
    private Integer stillTough;
}
