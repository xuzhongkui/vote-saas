package com.jm.vote.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.jm.vote.repository")
public class MyBatisPlusConfig {
}


