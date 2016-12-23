package com.airent.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    @Value("${airent.db.url}")
    private String dbUrl;

    @Value("${airent.db.name}")
    private String dbName;

    @Value("${airent.db.password}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");

        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbName);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setPassword(dbPassword);
        return dataSource;
    }


    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

}
