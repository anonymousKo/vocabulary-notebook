package me.kesx.tool.dao;

import me.kesx.tool.entity.Word;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface WordRepository extends CrudRepository<Word,Integer> {
    @Query(value = "select w from Word w where DATE(w.addDate)  = CURRENT_DATE() or" +
            " w.dateToRound like "+"%"+"?1"+"%")
    List<Word>  listToday(String today);

    @Query(value = "select w from Word w where w.hasMarked = 1")
    List<Word>  listMarked();
}