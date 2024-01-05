package net.lego.database.deployer.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(TargetDataSourceProperties.class)
@MapperScan(basePackages = {"net.lego.data.v2.mybatis.mapper"}, sqlSessionFactoryRef = "targetSqlSessionFactory")
@EnableTransactionManagement
public class MyBatisTargetDataSourceConfiguration {

    @Bean("targetDataSource")
    public DataSource targetDataSource(final TargetDataSourceProperties targetDataSourceProperties) {
        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .url(targetDataSourceProperties.determineUrl())
                                .username(targetDataSourceProperties.determineUsername())
                                .password(targetDataSourceProperties.determinePassword())
                                .driverClassName(targetDataSourceProperties.determineDriverClassName())
                                .build();
    }

    @Bean
    public SqlSessionFactory targetSqlSessionFactory(@Qualifier("targetDataSource")final DataSource targetDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(targetDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager targetDataSourceTransactionManager(@Qualifier("targetDataSource")final DataSource targetDataSource) {
        return new DataSourceTransactionManager(targetDataSource);
    }

    @Bean
    public SqlSessionTemplate targetSqlSessionTemplate(final SqlSessionFactory targetSqlSessionFactory) {
        return new SqlSessionTemplate(targetSqlSessionFactory);
    }
}
