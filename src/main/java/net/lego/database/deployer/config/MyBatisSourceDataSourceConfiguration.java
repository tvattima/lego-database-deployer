package net.lego.database.deployer.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"net.bricklink.data.lego.ibatis.mapper", "net.lego.data.v1.mybatis.mapper"}, sqlSessionTemplateRef = "sourceSqlSessionTemplate")
@EnableTransactionManagement
public class MyBatisSourceDataSourceConfiguration {

    @Bean
    @Primary
    public SqlSessionFactory sourceSqlSessionFactory(final DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    @Primary
    public PlatformTransactionManager sourceDataSourceTransactionManager(final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Primary
    public SqlSessionTemplate sourceSqlSessionTemplate(final SqlSessionFactory sourceSqlSessionFactory) {
        return new SqlSessionTemplate(sourceSqlSessionFactory);
    }
}
