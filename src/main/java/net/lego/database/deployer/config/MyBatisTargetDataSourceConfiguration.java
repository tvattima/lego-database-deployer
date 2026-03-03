package net.lego.database.deployer.config;

import io.legohunter.lego.data.builder.DataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Slf4j
@MapperScan(basePackages = {"net.lego.data.v2.mybatis.mapper"}, sqlSessionFactoryRef = "targetSqlSessionFactory")
@EnableTransactionManagement
@Configuration
public class MyBatisTargetDataSourceConfiguration {

    @Bean("targetDataSource")
    public DataSource targetDataSource(Map<String, Object> databasesMap, Environment environment) {
        String databaseKeyName = environment.getProperty("target-database.datasource.database-key-name");
        if (databaseKeyName == null) {
            throw new IllegalStateException("target-database.datasource.database-key-name property not set");
        }

        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        return dataSourceBuilder.dataSource(databaseKeyName, databasesMap, "target-database.datasource.hikari", environment);
    }

    @Bean
    public SqlSessionFactory targetSqlSessionFactory(@Qualifier("targetDataSource") final DataSource targetDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(targetDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager targetDataSourceTransactionManager(@Qualifier("targetDataSource") final DataSource targetDataSource) {
        return new DataSourceTransactionManager(targetDataSource);
    }

    @Bean
    public SqlSessionTemplate targetSqlSessionTemplate(final SqlSessionFactory targetSqlSessionFactory) {
        return new SqlSessionTemplate(targetSqlSessionFactory);
    }
}
