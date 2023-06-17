package com.taskscheduler.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class QuartzSchedulerConfig {

	private static final String QUARTZ_PROP_DATASOURCE_PREFIX = "org.quartz.dataSource.";

	private String url;
	private String username;
	private String password;

	/**
	 * create Scheduler Bean
	 * 
	 * @throws IOException
	 */
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext, DataSource quartzDataSource)
			throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setOverwriteExistingJobs(true);
		factory.setDataSource(quartzDataSource);
		factory.setQuartzProperties(quartzProperties());
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		factory.setJobFactory(jobFactory);
		return factory;
	}

	/**
	 * Configure quartz using properties file
	 * 
	 * @throws IOException
	 */
	public Properties quartzProperties() throws IOException {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));
		propertiesFactoryBean.afterPropertiesSet();
		Properties prop = propertiesFactoryBean.getObject();
		if (prop != null) {
			String quartzDataSourceName = prop.getProperty("org.quartz.jobStore.dataSource", "");
			prop.put(QUARTZ_PROP_DATASOURCE_PREFIX + quartzDataSourceName + ".URL", url);
			prop.put(QUARTZ_PROP_DATASOURCE_PREFIX + quartzDataSourceName + ".username", username);
			prop.put(QUARTZ_PROP_DATASOURCE_PREFIX + quartzDataSourceName + ".password", password);
		}
		return prop;
	}
}
