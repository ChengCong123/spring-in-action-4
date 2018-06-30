package spittr.data;

import java.util.List;

import spittr.Spittle;

public interface SpittleRepository {

  /**
   * 获取最近20条spittles
   * @return
   */
  List<Spittle> findRecentSpittles();

  /**
   * @param max 表明结果中所有Spittle的ID均应该在这个值之前（不包括max）
   * @param count 表明在结果中要包含的Spittle数量
   * @return
   */
  List<Spittle> findSpittles(long max, int count);

  /**
   *
   * @param id
   * @return
   */
  Spittle findOne(long id);

  /**
   *
   * @param spittle
   */
  void save(Spittle spittle);

}
