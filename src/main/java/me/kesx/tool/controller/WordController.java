package me.kesx.tool.controller;

import me.kesx.tool.common.Result;
import me.kesx.tool.entity.Word;
import me.kesx.tool.entity.WordVo;
import me.kesx.tool.service.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class WordController {

    @Autowired
    WordServiceImpl wordService;
    @Value("${forgetting.curve}")
    private String forgettingCurve;

    @RequestMapping(value = "/add")
    public Result<Word> add(@RequestBody WordVo req){
        return(Result.success(wordService.addNewWord(req,forgettingCurve)));
    }

    @RequestMapping(value = "/listToday")
    public Result<List<Word>> listToday(){
        return (Result.success(wordService.listToday()));
    }
    @RequestMapping(value = "/listMarked")
    public Result<List<Word>> listMarked(){
        return (Result.success(wordService.ListMarked()));
    }
}