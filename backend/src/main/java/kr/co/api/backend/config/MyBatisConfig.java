package kr.co.api.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@MapperScan(
        basePackages = "kr.co.api.backend.mapper",
        sqlSessionFactoryRef = "sqlSessionFactory"
)
public class MyBatisConfig {

    // ==========================================
    // 1. 데이터소스 생성 (Master / Slave)
    // ==========================================

    // Master DB (쓰기/읽기)
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // Slave DB (읽기 전용)
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // ==========================================
    // 2. 라우팅 설정 (신호등 역할)
    // ==========================================
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {

        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource);
        dataSourceMap.put("slave", slaveDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource); // 기본은 Master

        return routingDataSource;
    }

    // ==========================================
    // 3. 프록시 설정 (트랜잭션 타이밍 맞추기) - 중요!
    // ==========================================
    // 트랜잭션 시작 직후가 아니라, '실제 쿼리 실행 시점'에 데이터소스를 결정하도록 지연시킴
    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    // ==========================================
    // 4. 메인 SqlSessionFactory (라우팅 적용됨)
    // ==========================================
    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 매퍼 XML 위치 설정 (기존과 동일)
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mappers/**/*.xml")
        );
        // DTO 패키지 설정 (기존과 동일)
        sessionFactory.setTypeAliasesPackage("kr.co.api.backend.model, kr.co.api.backend.dto.search, kr.co.api.backend.dto");

        // CamelCase 설정
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        sessionFactory.setConfiguration(configuration);

        return sessionFactory.getObject();
    }

    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // =========================================================================
    // ★ [핵심] 동기화(Dual Write)를 위한 Slave 전용 접속 통로
    // =========================================================================
    // 이 빈(Bean)을 사용하면 라우팅 로직을 무시하고 "무조건 Slave DB"에 접속합니다.
    // 데이터 동기화 AOP에서 이 친구를 사용할 예정입니다.

    @Bean(name = "slaveSqlSessionFactory")
    public SqlSessionFactory slaveSqlSessionFactory(@Qualifier("slaveDataSource") DataSource slaveDataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(slaveDataSource); // 여기는 routingDataSource가 아니라 slaveDataSource를 직접 꽂음!

        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mappers/**/*.xml")
        );
        sessionFactory.setTypeAliasesPackage("kr.co.api.backend.model, kr.co.api.backend.dto.search, kr.co.api.backend.dto");

        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        sessionFactory.setConfiguration(configuration);

        return sessionFactory.getObject();
    }

    @Bean(name = "slaveSqlSessionTemplate")
    public SqlSessionTemplate slaveSqlSessionTemplate(@Qualifier("slaveSqlSessionFactory") SqlSessionFactory slaveSqlSessionFactory) {
        return new SqlSessionTemplate(slaveSqlSessionFactory);
    }
}