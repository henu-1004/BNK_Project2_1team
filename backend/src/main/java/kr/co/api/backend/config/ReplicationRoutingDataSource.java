package kr.co.api.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션의 속성(ReadOnly 여부)을 확인하여
 * Master DB 또는 Slave DB의 DataSource 키를 반환하는 라우팅 로직
 */
@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // 현재 트랜잭션이 'Read Only'인지 확인
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        // 디버깅용 로그 (나중에 운영 배포 시에는 주석 처리 권장)
         log.info("Current Transaction ReadOnly? : {}", isReadOnly);

        // readOnly가 true면 "slave", false(쓰기)면 "master" 키 반환
        return isReadOnly ? "slave" : "master";
    }
}