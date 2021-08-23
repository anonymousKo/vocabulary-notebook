package me.kesx.tool.entity;

import lombok.Data;

import java.util.Date;


@Data
public class WordVo {
    private Integer wordId;
    private String wordItem;
    private String pos;
    private String notes;
    private Integer hasMarked = 0;
    private Integer stillTough;
    private Integer round;
    private String needRememberDate;
}
