package me.kesx.tool.cache;

import me.kesx.tool.dao.WordRepository;
import me.kesx.tool.entity.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class WordDaoCache {

    @Autowired
    WordRepository wordRepository;

    List<Word> notFinishedWordList = new ArrayList<>();

    List<Word> toughWordList = new ArrayList<>();

    Set<String> wordItemSet = new HashSet<>();

    @PostConstruct
    public void init(){
        try{
            refreshNotFinished();
            refreshTough();
            refreshWordItem();
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

    public synchronized Set<String> getWordItem(){
        return wordItemSet;
    }

    @Scheduled
    public synchronized void refreshNotFinished(){
        notFinishedWordList =  wordRepository.listNotFinished();
    }
    public synchronized void refreshTough(){
        toughWordList =  wordRepository.listTough();
    }
    public synchronized void refreshWordItem(){
        wordItemSet =  wordRepository.listWordItem();
    }
}
