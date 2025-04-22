package com.eeerrorcode.lottomate.repository.projection.lotto;

/**
 * 번호와 그 번호의 출현 빈도를 담는 Projection 인터페이스입니다.
 *
 * <p>NumberFrequency Projection은 Native Query의 결과를 매핑하기 위한 용도로 사용되며,
 * 로또 당첨 번호 분포를 분석하는 데 사용됩니다. LottoRepository의 native Query에 기반합니다.</p>
 * 
 * <p>예: 최근 100회 기준, 숫자 17은 총 18회 등장함 → getNum() = 17, getFrequency() = 18</p>
 *
 * <h3>사용 예시</h3>
 * <pre>
 * List&lt;NumberFrequency&gt; frequencyList = repository.findNumberFrequenciesInRange(100L);
 * frequencyList.forEach(freq -&gt;
 *     System.out.println("번호: " + freq.getNum() + ", 등장횟수: " + freq.getFrequency()));
 * </pre>
 *
 * @author DahnDell
 */
public interface NumberFrequency {

  /**
   * 등장한 로또 번호를 반환합니다. (1~45)
   *
   * @return 로또 번호
   */
  Long getNum();

  /**
   * 해당 번호가 등장한 횟수를 반환합니다.
   *
   * @return 출현 횟수
   */
  Long getFrequency();
}
