package com.eeerrorcode.lottomate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class JacksonTimeModuleConfig implements WebMvcConfigurer {

  @Bean(name = "jacksonTemplateObjectMapper")
  public ObjectMapper jacksonTemplateObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Override
  public void extendMessageConverters(@NonNull List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
    for (org.springframework.http.converter.HttpMessageConverter<?> converter : converters) {
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        ((MappingJackson2HttpMessageConverter) converter).setObjectMapper(jacksonTemplateObjectMapper());
      }
    }
  }
}

