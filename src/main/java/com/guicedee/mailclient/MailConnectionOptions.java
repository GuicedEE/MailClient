package com.guicedee.mailclient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares SMTP connection-level configuration for a mail server.
 * Place on a class or {@code package-info.java}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface MailConnectionOptions
{
    /**
     * @return Logical name of this mail connection for bindings (used with {@code @Named}).
     */
    String value() default "default";

    /**
     * @return Hostname of the SMTP server.
     */
    String hostname() default "localhost";

    /**
     * @return Port of the SMTP server.
     */
    int port() default 25;

    /**
     * @return StartTLS mode: DISABLED, OPTIONAL, or REQUIRED.
     */
    StartTLSMode startTls() default StartTLSMode.OPTIONAL;

    /**
     * @return Login mode: DISABLED, NONE, or REQUIRED.
     */
    LoginMode login() default LoginMode.NONE;

    /**
     * @return Username for SMTP authentication.
     */
    String username() default "";

    /**
     * @return Password for SMTP authentication.
     */
    String password() default "";

    /**
     * @return Whether to use SSL on connect (port 465).
     */
    boolean ssl() default false;

    /**
     * @return Whether to trust all server certificates.
     */
    boolean trustAll() default false;

    /**
     * @return EHLO hostname for the mail client.
     */
    String ehloHostname() default "";

    /**
     * @return Space-separated list of allowed authentication methods.
     */
    String authMethods() default "";

    /**
     * @return Whether to enable connection pooling (keep-alive).
     */
    boolean keepAlive() default true;

    /**
     * @return Maximum number of connections in the pool.
     */
    int maxPoolSize() default 10;

    /**
     * @return Whether to allow recipient errors and continue sending.
     */
    boolean allowRcptErrors() default false;

    /**
     * @return Whether to disable ESMTP commands.
     */
    boolean disableEsmtp() default false;

    /**
     * @return Mail user agent name for boundaries and message-id.
     */
    String userAgent() default "";

    /**
     * @return Whether to enable DKIM signing.
     */
    boolean enableDkim() default false;

    /**
     * @return Whether to enable pipelining.
     */
    boolean pipelining() default true;

    /**
     * @return Maximum emails per connection before closing.
     */
    long maxMailsPerConnection() default 0;

    /**
     * @return Keep-alive timeout in seconds.
     */
    int keepAliveTimeout() default 300;

    /**
     * @return Default "from" address for all messages sent via this connection.
     */
    String defaultFrom() default "";

    /**
     * StartTLS modes mapping to Vert.x StartTLSOptions.
     */
    enum StartTLSMode
    {
        DISABLED,
        OPTIONAL,
        REQUIRED
    }

    /**
     * Login modes mapping to Vert.x LoginOption.
     */
    enum LoginMode
    {
        DISABLED,
        NONE,
        REQUIRED
    }
}

