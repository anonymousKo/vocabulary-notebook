package me.kesx.tool.service;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import me.kesx.tool.cache.WordDaoCache;
import me.kesx.tool.dao.WordRepository;
import me.kesx.tool.entity.Word;
import me.kesx.tool.entity.WordRound;
import me.kesx.tool.entity.WordVo;
import me.kesx.tool.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WordServiceImpl {
    @Autowired
    DateUtil dateUtil;
    @Autowired
    WordRepository wordRepository;
    @Autowired
    WordDaoCache wordDaoCache;

    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    public Word addNewWord(WordVo req,String forgettingCurve){
        if (wordDaoCache.getWordItem().contains(req.getWordItem())){
            return queryByWordItem(req);
        }
        List<Object> wordRoundList = new ArrayList<>();
        Word word = new Word();
        BeanUtils.copyProperties(req,word);
        word.setAddDate(dateUtil.readableDateFormat(new Date()));
        calculateRememberDate(forgettingCurve,wordRoundList);
        log.info("the word is:{}, rememberDate is:{}",word.getWordItem(),wordRoundList);
        Gson gson = new Gson();
        word.setDateToHasMarked(gson.toJson(wordRoundList));
        wordRepository.save(word);
        runBackground(() -> {
            wordDaoCache.refreshWordItem();
            wordDaoCache.refreshNotFinished();
        });
        return null;
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

    private Word queryByWordItem(WordVo req){
        return wordRepository.queryByWordItem(req.getWordItem());
    }

    private List<WordRound>  analyzeDateToHasMarked(List<WordRound> wordRoundList ,Word word){
        Gson gson = new Gson();
        try{
            wordRoundList =  gson.fromJson(word.getDateToHasMarked(), new TypeToken<ArrayList<WordRound>>(){}.getType());
        }catch (Exception e ){
            log.info(e.toString());
        }
        return wordRoundList;
    }

    public Map<String,List<WordVo>>  listToday(){
        String today = dateUtil.dateFormat(new Date());
        List<Word> notFinishedWordList =  wordDaoCache.getAllNotFinished();
        log.info("find not finished word ->{}",notFinishedWordList);
        List<WordVo> todayWordList =  new ArrayList<>();
        List<WordVo> remainWordList =  new ArrayList<>();
        Map<String,List<WordVo>> isTodayToWordMap = new HashMap<>();
        notFinishedWordList.forEach(word -> {
            if(DateUtils.isSameDay(word.getAddDate(),new Date())){
                WordVo wordVo = new WordVo();
                BeanUtils.copyProperties(word,wordVo);
                wordVo.setRound(0);
                todayWordList.add(wordVo);
            }
            List<WordRound>  wordRoundList = new ArrayList<>();
            wordRoundList = analyzeDateToHasMarked(wordRoundList,word);
            wordRoundList.forEach(wordRound -> {
                if(wordRound.getRememberDate().equals(today) ){
                    WordVo wordVo = new WordVo();
                    BeanUtils.copyProperties(word,wordVo);
                    wordVo.setRound(wordRound.getRound());
                    todayWordList.add(wordVo);
                }
                else if (wordRound.getHasMarked() != 1 && dateUtil.compareStringDateSmaller(wordRound.getRememberDate(), today)){
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
        log.info("todayWords ->{};    remainWords->{}",todayWordList.stream().map(WordVo::getWordItem).collect(Collectors.toList()),
                remainSortedWordList.stream().map(WordVo::getWordItem).collect(Collectors.toList()));
        isTodayToWordMap.put("today",todayWordList);
        isTodayToWordMap.put("remain",remainSortedWordList);
        return  isTodayToWordMap;
    }

    public List<Word> listTough(){
        return wordDaoCache.getAllTough();
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
            int result = wordRepository.updateMarked(req.getWordId(),jsonElement.toString());
            runBackground(() -> {wordDaoCache.refreshNotFinished();} );
            return result;
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

    public int addToughWord(WordVo req){
        int result = wordRepository.addToughWord(req.getWordId());
        runBackground(() -> {wordDaoCache.refreshTough();} );
        return result;
    }
    public int removeToughWord(WordVo req){
        int result = wordRepository.removeToughWord(req.getWordId());
        runBackground(() -> {wordDaoCache.refreshTough();} );
        return result;
    }
    public void deleteWord(WordVo req){
        runBackground(() -> {wordDaoCache.refreshNotFinished();} );
        wordRepository.deleteById(req.getWordId());
    }
    public int updateDetail(WordVo req){
        int result = wordRepository.updateDetail(req.getNotes(),req.getPos(),req.getWordItem(),req.getWordId());
        runBackground(() -> {wordDaoCache.refreshNotFinished();} );
        return result;
    }

    public   void runBackground(Runnable runnable){
        scheduled.schedule(runnable , 0,TimeUnit.SECONDS);
    }
}
