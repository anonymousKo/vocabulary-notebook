package me.kesx.tool.service;


import com.google.gson.Gson;
import me.kesx.tool.dao.WordRepository;
import me.kesx.tool.entity.Word;
import me.kesx.tool.entity.WordVo;
import me.kesx.tool.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordServiceImpl {
    @Autowired
    DateUtil dateUtil;
    @Autowired
    WordRepository wordRepository;

    Map<String,Integer> DateToRound = new HashMap<>();

    public Word addNewWord(WordVo req,String forgettingCurve){
        Word word = new Word();
        convertWordVoToWord(req,word);
        word.setAddDate(dateUtil.readableDateFormat(new Date()));
        calculateRememberDate(forgettingCurve);
        Gson gson = new Gson();
        word.setDateToRound(gson.toJson(DateToRound));
        return wordRepository.save(word);
    }
    public void calculateRememberDate(String forgettingCurve){
        String[] cycle = forgettingCurve.split(" ");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        for(int i =0;i < cycle.length;i++){
            calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(cycle[i]));
            String rememberDate = dateUtil.dateFormat(calendar.getTime());
            DateToRound.put(rememberDate,i+1);
        }
    }
    private void convertWordVoToWord(WordVo wordVo,Word word){
        word.setWord(wordVo.getWord());
        word.setNotes(wordVo.getNotes());
        word.setPos(wordVo.getPos());
    }

    public List<Word> listToday(){
        String today = dateUtil.dateFormat(new Date());
        return wordRepository.listToday(today);
    }

    public List<Word> ListMarked(){
        return wordRepository.listMarked();
    }
}
