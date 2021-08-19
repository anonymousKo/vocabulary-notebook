package me.kesx.tool.dao;

import me.kesx.tool.entity.Word;
import me.kesx.tool.entity.WordVo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface WordRepository extends CrudRepository<Word,Integer> {
    @Query(value = "select w from Word w where Date(w.addDate)  = CURRENT_DATE() or" +
            " w.dateToRound like "+"%"+"?1"+"%")
    List<Word>  listToday(String today);

    @Query(value = "select w from Word w where w.hasMarked = 1")
    List<Word>  listMarked();

    @Modifying
    @Query(value = "update Word word set word.hasMarked= :hasMarked where word.wordId = :wordId")
    int updateMarked( Integer wordId, Integer hasMarked);

    @Modifying
    @Query(value = "update Word word set word.stillTough= :stillTough where word.wordId = :wordId")
    int updateTough( Integer wordId, Integer stillTough);

    @Modifying
    @Query(value = "update Word word set word.wordItem= :wordItem,word.notes= :notes,word.pos= :pos where word.wordId = :wordId")
    int updateDetail(String notes,String pos,String wordItem,Integer wordId);
}
