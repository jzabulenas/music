package com.example.demo.security;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class MagicLinkSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

  private final JavaMailSender mailSender;

  @Value("${app.backend-url}")
  private String backendUrl;

  MagicLinkSuccessHandler(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void handle(
    HttpServletRequest request,
    HttpServletResponse response,
    OneTimeToken oneTimeToken
  ) throws IOException, ServletException {
    String magicLink = UriComponentsBuilder.fromUriString(this.backendUrl)
      .path("/login/ott")
      .queryParam("token", oneTimeToken.getTokenValue())
      .build()
      .toUriString();

    try {
      MimeMessage message = this.mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

      helper.setTo(oneTimeToken.getUsername());
      helper.setFrom("noreply@music-rec.local");
      helper.setSubject("Your sign-in link");
      helper.setText(
        """
        <p>Click the link below to sign in. It expires in 5 minutes.</p>
        <p><a href="%s">Sign in</a></p>
        """.formatted(magicLink),
        true
      );

      this.mailSender.send(message);
    } catch (MessagingException e) {
      throw new ServletException("Failed to send magic link email", e);
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }
}
