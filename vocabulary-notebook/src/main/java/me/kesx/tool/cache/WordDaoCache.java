package me.kesx.tool.cache;

import me.kesx.tool.dao.WordRepository;
import me.kesx.tool.entity.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordDaoCache {

    @Autowired
    WordRepository wordRepository;

    List<Word> notFinishedWordList = new ArrayList<>();

    List<Word> toughWordList = new ArrayList<>();

    @PostConstruct
    public void init(){
        try{
            refreshNotFinished();
            refreshTough();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized List<Word>  getAllNotFinished(){
        return notFinishedWordList;
    }

    public synchronized List<Word>  getAllTough(){
        return toughWordList;
    }

    @Scheduled
    public synchronized void refreshNotFinished(){
        notFinishedWordList =  wordRepository.listNotFinished();
    }
    public synchronized void refreshTough(){
        toughWordList =  wordRepository.listTough();
    }
}
