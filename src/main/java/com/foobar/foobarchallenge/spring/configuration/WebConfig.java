package com.foobar.foobarchallenge.spring.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vavr.jackson.datatype.VavrModule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

  public XmlMapper webXmlMapper() {
    JacksonXmlModule xmlModule = new JacksonXmlModule();
    xmlModule.setDefaultUseWrapper(false);
    XmlMapper xmlMapper = new XmlMapper(xmlModule);
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    xmlMapper.setVisibility(
        VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    xmlMapper.registerModule(new ParameterNamesModule());
    xmlMapper.registerModule(new Jdk8Module());
    xmlMapper.registerModule(new JavaTimeModule());
    xmlMapper.registerModule(new VavrModule());
    return xmlMapper;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(xmlConverter());
    converters.add(jsonConverter());
  }

  @Bean
  public MappingJackson2XmlHttpMessageConverter xmlConverter() {
    return new MappingJackson2XmlHttpMessageConverter(webXmlMapper());
  }

  @Bean
  public MappingJackson2HttpMessageConverter jsonConverter() {
    return new MappingJackson2HttpMessageConverter(objectMapper());
  }

  @Bean
  public ViewResolver contentNegotiatingViewResolver() {
    ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
    viewResolver.setDefaultViews(getDefaultViews());
    return viewResolver;
  }

  private List<View> getDefaultViews() {
    List<View> defaultViews = new ArrayList<>();
    defaultViews.add(new MappingJackson2JsonView());
    defaultViews.add(new MappingJackson2XmlView());
    // Add other views if needed
    return defaultViews;
  }
}
