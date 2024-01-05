package net.lego.database.deployer.config;

import com.zaxxer.hikari.HikariDataSource;
import net.bricklink.data.lego.ibatis.configuration.DataSourceProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@MapperScan(basePackages = {"net.bricklink.data.lego.ibatis.mapper", "net.lego.data.v1.mybatis.mapper"}, sqlSessionTemplateRef = "sourceSqlSessionTemplate")
@EnableTransactionManagement
public class MyBatisSourceDataSourceConfiguration {

    @Bean("sourceDataSource")
    @Primary
    public DataSource sourceDataSource(final DataSourceProperties dataSourceProperties) {
        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .url(dataSourceProperties.determineUrl())
                                .username(dataSourceProperties.determineUsername())
                                .password(dataSourceProperties.determinePassword())
                                .driverClassName(dataSourceProperties.determineDriverClassName())
                                .build();
    }

    @Bean
    @Primary
    public SqlSessionFactory sourceSqlSessionFactory(@Qualifier("sourceDataSource") final DataSource sourceDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(sourceDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    @Primary
    public PlatformTransactionManager sourceDataSourceTransactionManager(@Qualifier("sourceDataSource") final DataSource sourceDataSource) {
        return new DataSourceTransactionManager(sourceDataSource);
    }

    @Bean
    @Primary
    public SqlSessionTemplate sourceSqlSessionTemplate(final SqlSessionFactory sourceSqlSessionFactory) {
        return new SqlSessionTemplate(sourceSqlSessionFactory);
    }
}
