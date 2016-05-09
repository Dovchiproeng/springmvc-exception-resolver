package com.vdp.spring.bootstrap;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fasterxml.jackson.databind.MapperFeature;
import com.vdp.spring.bootstrap.rest.RestJsonExceptionResolver;


@Configuration
@ComponentScan(basePackages = "com.vdp.spring.controller")
@EnableWebMvc
public class MvcConfig extends WebMvcConfigurerAdapter{

	@Bean
    public InternalResourceViewResolver htmlViewResolver() {
        final InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(InternalResourceView.class);
        bean.setOrder(999);
        bean.setPrefix("/WEB-INF/");
        bean.setSuffix("");
        return bean;
    }

	/** To excluded the null filed when response to user*/
	@Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        final SkipNullObjectMapper skipNullMapper = new SkipNullObjectMapper();
        skipNullMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        skipNullMapper.setDateFormat(formatter);
        skipNullMapper.init();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(skipNullMapper);
        converters.add(converter);
    }

	/** Register RestJsonExceptionResolver */
	@Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(restJsonExceptionResolver());
    }

	@Bean
    public RestJsonExceptionResolver restJsonExceptionResolver() {
	    RestJsonExceptionResolver bean = new RestJsonExceptionResolver();
	    // map any spring and servlet exception class here
	    RestJsonExceptionResolver.registerExceptionWithHTTPCode(org.springframework.beans.TypeMismatchException.class, 400);
        RestJsonExceptionResolver.registerExceptionWithHTTPCode(MissingServletRequestParameterException.class, 400);
        RestJsonExceptionResolver.registerExceptionWithHTTPCode(MethodArgumentNotValidException.class, 400);
        RestJsonExceptionResolver.registerExceptionWithHTTPCode(ServletRequestBindingException.class, 400);
        bean.setOrder(100);
	    return bean;
    }

}