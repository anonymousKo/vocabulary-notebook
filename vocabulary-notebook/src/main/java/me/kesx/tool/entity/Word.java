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
    private String wordItem;
    private String pos;
    private String notes;
    private Date addDate;
    private String dateToHasMarked;
    private Integer stillTough=0;
    private Integer finished=0;
}
