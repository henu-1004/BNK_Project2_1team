package kr.co.api.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [계정계 업무 식별용 어노테이션]
 * * 이 어노테이션이 붙은 메서드(Mapper)는 AOP에 의해 감지되어
 * Master DB에 쓰기 작업(Insert/Update/Delete) 수행 후,
 * 즉시 Slave DB에도 동일한 작업을 수행합니다. (Dual Write / 동기식)
 * * 대상: 계좌 개설, 입금, 출금, 이체 등 데이터 무결성이 중요한 업무
 */
@Target(ElementType.METHOD)        // 메서드 위에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME) // 실행 중(Runtime)에도 이 표시가 남아있어야 AOP가 읽을 수 있음
public @interface CoreBanking {
}