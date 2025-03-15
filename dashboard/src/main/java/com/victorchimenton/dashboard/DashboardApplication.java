package com.victorchimenton.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.victorchimenton.dashboard", "com.victorchimenton.core"})
public class DashboardApplication {
  public static void main(String[] args) {
    SpringApplication.run(DashboardApplication.class, args);
  }
}
