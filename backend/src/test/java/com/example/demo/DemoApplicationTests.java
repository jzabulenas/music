package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

  @MockitoBean
  @SuppressWarnings("unused")
  private JavaMailSender mailSender;

  @Test
  void contextLoads() {}
}
