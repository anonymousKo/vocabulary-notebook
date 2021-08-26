package me.kesx.tool.dao;

import me.kesx.tool.entity.Word;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Transactional
public interface WordRepository extends CrudRepository<Word,Integer> {
//    @Query(value = "select w from Word w where Date(w.addDate)  = CURRENT_DATE() or" +
//            " w.dateToRound like "+"%"+"?1"+"%")
//    List<Word>  listToday(String today);

    @Query(value = "select word from Word word where word.finished = 0")
    List<Word>  listNotFinished();

    @Query(value = "select w from Word w where w.stillTough = 1")
    List<Word> listTough();

    @Modifying
    @Query(value = "update Word word set word.dateToHasMarked= :updatedHasMarked where word.wordId = :wordId")
    Integer updateMarked( Integer wordId,String updatedHasMarked);

    @Modifying
    @Query(value = "update Word word set word.finished= :isFinished where word.wordId = :wordId")
    Integer updateFinished( Integer wordId,Integer isFinished);

    @Modifying
    @Query(value = "update Word word set word.stillTough= 1 where word.wordId = :wordId")
    Integer addToughWord( Integer wordId);

    @Modifying
    @Query(value = "update Word word set word.stillTough= 0 where word.wordId = :wordId")
    Integer removeToughWord( Integer wordId);

    @Modifying
    @Query(value = "update Word word set word.wordItem= :wordItem,word.notes= :notes,word.pos= :pos where word.wordId = :wordId")
    Integer updateDetail(String notes,String pos,String wordItem,Integer wordId);

    @Query(value = "select word.wordItem from Word word")
    Set<String> listWordItem();

    @Query(value = "select word from Word word where word.wordItem= :wordItem")
    Word queryByWordItem(String wordItem);
}
