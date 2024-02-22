package io.cockroachdb.test.spring3;

import javax.sql.DataSource;

import org.postgresql.PGProperty;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@EnableTransactionManagement(order = Ordered.LOWEST_PRECEDENCE - 1)
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableJpaRepositories(basePackageClasses = SpringApp.class, enableDefaultTransactions = false)
@ComponentScan(basePackageClasses = SpringApp.class)
@Configuration
public class SpringApp {
    @Bean
    @Primary
    public DataSource primaryDataSource(DataSourceProperties properties) {
        HikariDataSource ds = hikariDataSource(properties);
        return ProxyDataSourceBuilder
                .create(ds)
                .logQueryBySlf4j(SLF4JLogLevel.TRACE, "io.cockroachdb.SQL_TRACE")
                .asJson()
                .multiline()
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource hikariDataSource(DataSourceProperties properties) {
        HikariDataSource ds = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.addDataSourceProperty(PGProperty.REWRITE_BATCHED_INSERTS.getName(), "true");
        ds.addDataSourceProperty(PGProperty.APPLICATION_NAME.getName(), "cockroachdb-test");
        return ds;
    }
}
