package com.nhjclxc.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


// 基于 Velocity 模板的代码生成
//@SpringBootApplication
@SpringBootApplication(scanBasePackages = {"com.nhjclxc.generator"},
        exclude={DataSourceAutoConfiguration.class})
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }

}
