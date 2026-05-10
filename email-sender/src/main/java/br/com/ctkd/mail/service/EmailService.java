package br.com.ctkd.mail.service;

import br.com.ctkd.mail.config.SesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient;
    private final SesProperties properties;

    public void sendWelcome(String destino, String nome, String linkAcesso) {
        String html = """
                <html>
                  <body>
                    <h1>Bem-vindo, %s!</h1>
                    <p>Acesse sua conta: <a href="%s">%s</a></p>
                  </body>
                </html>
                """.formatted(nome, linkAcesso, linkAcesso);

        send(destino, "Bem-vindo à CarbigData!", html);
    }

    private void send(String destino, String assunto, String html) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(properties.from())
                    .destination(d -> d.toAddresses(destino))
                    .message(m -> m
                            .subject(s -> s
                                    .data(assunto)
                                    .charset("UTF-8"))
                            .body(b -> b
                                    .html(h -> h
                                            .data(html)
                                            .charset("UTF-8"))))
                    .build();

            sesClient.sendEmail(request);
            log.info("Email enviado para: {}", destino);

        } catch (SesException e) {
            log.error("Erro ao enviar email para {}: {}", destino, e.getMessage());
            throw new RuntimeException("Falha no envio do email", e);
        }
    }
}
