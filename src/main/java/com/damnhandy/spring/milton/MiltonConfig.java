package com.damnhandy.spring.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.ResourceFactory;
import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.http.fs.NullSecurityManager;
import io.milton.http.http11.DefaultHttp11ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 *
 */
@Configuration
@EnableConfigurationProperties(MiltonProperties.class)
public class MiltonConfig {

    @Autowired
    MiltonProperties miltonProperties;

    @Bean
    FilterRegistrationBean filterRegistrationBean()
    {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(springMiltonFilterBean());
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Bean
    SpringMiltonFilterBean springMiltonFilterBean()
    {
        return new SpringMiltonFilterBean();
    }

    @Bean
    @ConditionalOnProperty("milton.filesystem_root")
    ResourceFactory resourceFactory()
    {
        FileSystemResourceFactory fs = new FileSystemResourceFactory();
        fs.setRoot(new File(miltonProperties.getFilesystemRoot()));
        fs.setSecurityManager(new NullSecurityManager());
        return fs;
    }

    @Bean
    HttpManagerBuilder httpManagerBuilder()
    {
        HttpManagerBuilder builder = new HttpManagerBuilder();
        builder.setResourceFactory(resourceFactory());
        builder.setBuffering(DefaultHttp11ResponseHandler.BUFFERING.never);
        builder.setEnableCompression(false);
        return builder;
    }
}
