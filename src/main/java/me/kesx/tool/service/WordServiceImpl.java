package me.kesx.tool.service;


import com.google.gson.Gson;
import me.kesx.tool.dao.WordRepository;
import me.kesx.tool.entity.Word;
import me.kesx.tool.entity.WordVo;
import me.kesx.tool.util.DateUtil;
import org.springframework.beans.BeanUtils;
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
        BeanUtils.copyProperties(req,word);
        word.setAddDate(dateUtil.readableDateFormat(new Date()));
        calculateRememberDate(forgettingCurve);
        Gson gson = new Gson();
        word.setDateToRound(gson.toJson(DateToRound));
        return wordRepository.save(word);
    }
    private void calculateRememberDate(String forgettingCurve){
        String[] cycle = forgettingCurve.split(" ");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for(int i =0;i < cycle.length;i++){
            calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(cycle[i]));
            String rememberDate = dateUtil.dateFormat(calendar.getTime());
            DateToRound.put(rememberDate,i+1);
        }
    }

    public List<Word> listToday(){
        String today = dateUtil.dateFormat(new Date());
        List<Word> todayWordList = wordRepository.listToday(today);
        todayWordList.forEach(todayWord ->{
            todayWord.getDateToRound();
            Gson gson = new Gson();
            Map map = gson.fromJson(jsonString, Map.class);
            map.get(today);
        });
    }

    public List<Word> ListMarked(){
        return wordRepository.listMarked();
    }

    public int updateMarkedWord(WordVo req){
         return(wordRepository.updateMarked(req.getWordId(),req.getHasMarked()));
    }
    public int updateToughWord(WordVo req){
        return wordRepository.updateTough(req.getWordId(),req.getStillTough());
    }
    public void deleteWord(WordVo req){
        wordRepository.deleteById(req.getWordId());
    }
    public int updateDetail(WordVo req){
        return wordRepository.updateDetail(req.getNotes(),req.getPos(),req.getWordId());
    }
}
