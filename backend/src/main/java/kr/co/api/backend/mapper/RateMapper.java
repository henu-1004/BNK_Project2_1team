package kr.co.api.backend.mapper;

import kr.co.api.backend.dto.RateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;


@Mapper
public interface RateMapper {


     // 오늘 날짜 기준, 해당 통화 환율 저장 돼 있는지 확인

    int existsTodayRate(@Param("currency") String currency,
                        @Param("regDt") LocalDate regDt);

    // 환율 데이터 저장

    int insertRate(RateDTO rateDTO);



}
