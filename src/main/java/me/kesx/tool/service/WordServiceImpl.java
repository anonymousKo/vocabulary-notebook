package me.kesx.tool.service;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import me.kesx.tool.dao.WordRepository;
import me.kesx.tool.entity.Word;
import me.kesx.tool.entity.WordRound;
import me.kesx.tool.entity.WordVo;
import me.kesx.tool.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WordServiceImpl {
    @Autowired
    DateUtil dateUtil;
    @Autowired
    WordRepository wordRepository;

    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    LinkedBlockingQueue<WordVo> editMarkedQueue = new LinkedBlockingQueue<>(100);

    @PostConstruct
    private void init(){
        List<WordVo> x = new ArrayList<>();
        editMarkedQueue.drainTo(x,50);
        if(x.size() > 0){
            if(x.size()%2 == 1){

            }
        }
    }
    public Word addNewWord(WordVo req,String forgettingCurve){
        List<Object> wordRoundList = new ArrayList<>();
        Word word = new Word();
        BeanUtils.copyProperties(req,word);
        word.setAddDate(dateUtil.readableDateFormat(new Date()));
        calculateRememberDate(forgettingCurve,wordRoundList);
        log.info("the word is:{}, rememberDate is:{}",word.getWordItem(),wordRoundList);
        Gson gson = new Gson();
        word.setDateToHasMarked(gson.toJson(wordRoundList));
        return wordRepository.save(word);
    }

    private void calculateRememberDate(String forgettingCurve, List<Object> wordRoundList){
        String[] cycles = forgettingCurve.split(" ");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for (int i = 0 ;i < cycles.length ; i++) {
            Map<String,String> wordRoundMap = new HashMap<>();
            calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(cycles[i]));
            String rememberDate = dateUtil.dateFormat(calendar.getTime());
            wordRoundMap.put("rememberDate", rememberDate);
            wordRoundMap.put("round", String.valueOf(i+1));
            wordRoundMap.put("hasMarked", "0");
            wordRoundList.add(wordRoundMap);
        }
    }

    public Map<String,List<WordVo>>  listToday(){
        String today = dateUtil.dateFormat(new Date());
        List<Word> notFinishedWordList =  wordRepository.listNotFinished();
        log.info("find not finished word ->{}",notFinishedWordList);
        List<WordVo> todayWordList =  new ArrayList<>();
        List<WordVo> remainWordList =  new ArrayList<>();
        Map<String,List<WordVo>> isTodayToWordMap = new HashMap<>();
        Gson gson = new Gson();
        notFinishedWordList.forEach(word -> {
            if(DateUtils.isSameDay(word.getAddDate(),new Date()) || word.getDateToHasMarked().contains(today)){
                WordVo wordVo = new WordVo();
                BeanUtils.copyProperties(word,wordVo);
                wordVo.setRound(0);
                todayWordList.add(wordVo);
            }
            List<WordRound>  wordRoundList = new ArrayList<>();
            try{
                wordRoundList =  gson.fromJson(word.getDateToHasMarked(), new TypeToken<ArrayList<WordRound>>(){}.getType());
            }catch (Exception e ){
                log.info(e.toString());
            }
            wordRoundList.forEach(wordRound -> {
                if (wordRound.getHasMarked() != 1 && dateUtil.compareStringDateSmaller(wordRound.getRememberDate(), today)){
                    WordVo wordVo = new WordVo();
                    BeanUtils.copyProperties(word,wordVo);
                    wordVo.setRound(wordRound.getRound());
                    wordVo.setNeedRememberDate(wordRound.getRememberDate());
                    remainWordList.add(wordVo);
                }
            });
        });
        List<WordVo> remainSortedWordList = remainWordList.stream().sorted(
                Comparator.comparing(WordVo::getNeedRememberDate)).collect(Collectors.toList());
        log.info("find the todayWords ->{};    the remainWordList->{}",todayWordList,remainSortedWordList);
        isTodayToWordMap.put("today",todayWordList);
        isTodayToWordMap.put("remain",remainSortedWordList);
        return  isTodayToWordMap;
    }

    public List<Word> ListMarked(){
        return wordRepository.listMarked();
    }

    public int updateMarkedWord(WordVo req){
        Word word = wordRepository.findById(req.getWordId()).orElse(null);
        if(word != null){
            JsonElement jsonElement = new Gson().fromJson(word.getDateToHasMarked(), JsonElement.class);
            jsonElement.getAsJsonArray().forEach(jsonSubElement -> {
                int oldHasMarked = jsonSubElement.getAsJsonObject().get("hasMarked").getAsInt();
                if(jsonSubElement.getAsJsonObject().get("rememberDate").getAsString().equals(req.getNeedRememberDate())){
                    jsonSubElement.getAsJsonObject().add("hasMarked", new JsonPrimitive((oldHasMarked + 1)&1) );
                }
            });
            scheduled.schedule(() -> checkFinished(word, jsonElement), 10 * 1000, TimeUnit.MILLISECONDS);
            return wordRepository.updateMarked(req.getWordId(),jsonElement.toString());
        }
        return 0;
    }

    private void checkFinished(Word word,JsonElement jsonElement){
        AtomicInteger markedTimes = new AtomicInteger();
        if(word.getFinished() != 1){
            jsonElement.getAsJsonArray().forEach(jsonSubElement -> {
                if(jsonSubElement.getAsJsonObject().get("hasMarked").getAsInt() == 1){
                    markedTimes.getAndIncrement();
                }
            });
            if (markedTimes.get() == 5){
                wordRepository.updateFinished(word.getWordId(),1);
            }
        }else{
            jsonElement.getAsJsonArray().forEach(jsonSubElement -> {
                if(jsonSubElement.getAsJsonObject().get("hasMarked").getAsInt() == 1){
                    markedTimes.getAndIncrement();
                }
            });
            if (markedTimes.get() < 5){
                wordRepository.updateFinished(word.getWordId(),0);
            }
        }
    }

    public int updateToughWord(WordVo req){
        return wordRepository.updateTough(req.getWordId(),req.getStillTough());
    }
    public void deleteWord(WordVo req){
        wordRepository.deleteById(req.getWordId());
    }
    public int updateDetail(WordVo req){
        return wordRepository.updateDetail(req.getNotes(),req.getPos(),req.getWordItem(),req.getWordId());
    }
}
