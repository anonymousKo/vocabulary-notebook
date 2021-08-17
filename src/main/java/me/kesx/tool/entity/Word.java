package me.kesx.tool.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "vocabulary")
@Entity
public class Word {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer wordId;
    private String word;
    private String pos;
    private String notes;
    private Date addDate;
    private Integer hasMarked;
    private String dateToRound;
    private Integer stillTough;
}
