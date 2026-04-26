package com.guicedee.mailclient;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Sends emails via a named SMTP connection. Bound via Guice with {@code @Named("connection-name")}.
 * <p>
 * Wraps a Vert.x {@link MailClient} and provides convenience methods for building and sending messages.
 */
@JsonSerialize(as = Void.class)
@EqualsAndHashCode(of = {"connectionName"})
@Log4j2
public class MailService
{
    private final MailClient mailClient;
    private final String connectionName;
    private final String defaultFrom;

    /**
     * Creates a mail service bound to a specific connection.
     *
     * @param mailClient     The Vert.x mail client.
     * @param connectionName The logical connection name.
     * @param defaultFrom    The default from address (may be empty).
     */
    public MailService(MailClient mailClient, String connectionName, String defaultFrom)
    {
        this.mailClient = mailClient;
        this.connectionName = connectionName;
        this.defaultFrom = defaultFrom;
    }

    /**
     * Sends a pre-built {@link MailMessage}.
     *
     * @param message The mail message to send.
     * @return A future with the mail result.
     */
    public Future<MailResult> send(MailMessage message)
    {
        if ((message.getFrom() == null || message.getFrom().isBlank()) && !defaultFrom.isBlank())
        {
            message.setFrom(defaultFrom);
        }
        return mailClient.sendMail(message)
                .onSuccess(result -> log.debug("Mail sent via connection '{}', messageId={}, recipients={}",
                        connectionName, result.getMessageID(), result.getRecipients()))
                .onFailure(t -> log.error("Failed to send mail via connection '{}'", connectionName, t));
    }

    /**
     * Sends a simple text email.
     *
     * @param from    Sender address.
     * @param to      Recipient address.
     * @param subject Email subject.
     * @param text    Plain text body.
     * @return A future with the mail result.
     */
    public Future<MailResult> sendText(String from, String to, String subject, String text)
    {
        MailMessage message = new MailMessage()
                .setFrom(from)
                .setTo(to)
                .setSubject(subject)
                .setText(text);
        return send(message);
    }

    /**
     * Sends a simple text email using the default from address.
     *
     * @param to      Recipient address.
     * @param subject Email subject.
     * @param text    Plain text body.
     * @return A future with the mail result.
     */
    public Future<MailResult> sendText(String to, String subject, String text)
    {
        return sendText(defaultFrom, to, subject, text);
    }

    /**
     * Sends an HTML email.
     *
     * @param from    Sender address.
     * @param to      Recipient address.
     * @param subject Email subject.
     * @param html    HTML body.
     * @return A future with the mail result.
     */
    public Future<MailResult> sendHtml(String from, String to, String subject, String html)
    {
        MailMessage message = new MailMessage()
                .setFrom(from)
                .setTo(to)
                .setSubject(subject)
                .setHtml(html);
        return send(message);
    }

    /**
     * Sends an HTML email using the default from address.
     *
     * @param to      Recipient address.
     * @param subject Email subject.
     * @param html    HTML body.
     * @return A future with the mail result.
     */
    public Future<MailResult> sendHtml(String to, String subject, String html)
    {
        return sendHtml(defaultFrom, to, subject, html);
    }

    /**
     * Sends a multipart email (text + HTML).
     *
     * @param from    Sender address.
     * @param to      Recipient addresses.
     * @param subject Email subject.
     * @param text    Plain text body.
     * @param html    HTML body.
     * @return A future with the mail result.
     */
    public Future<MailResult> sendMultipart(String from, List<String> to, String subject, String text, String html)
    {
        MailMessage message = new MailMessage()
                .setFrom(from)
                .setTo(to)
                .setSubject(subject)
                .setText(text)
                .setHtml(html);
        return send(message);
    }

    /**
     * @return The underlying Vert.x {@link MailClient}.
     */
    public MailClient getMailClient()
    {
        return mailClient;
    }

    /**
     * @return The logical connection name.
     */
    public String getConnectionName()
    {
        return connectionName;
    }

    /**
     * @return The default from address for this connection.
     */
    public String getDefaultFrom()
    {
        return defaultFrom;
    }
}

