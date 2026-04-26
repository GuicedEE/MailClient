package com.guicedee.mailclient.implementations;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.guicedee.client.services.lifecycle.IGuiceModule;
import com.guicedee.mailclient.MailConnectionOptions;
import com.guicedee.mailclient.MailService;
import com.guicedee.vertx.spi.VertXPreStartup;
import io.vertx.ext.mail.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

/**
 * Guice module that binds {@link MailClient} and {@link MailService} instances
 * for each discovered {@link MailConnectionOptions}.
 */
@Log4j2
public class MailClientModule extends AbstractModule implements IGuiceModule<MailClientModule>
{
    @Getter
    private static final Map<String, MailClient> mailClients = new HashMap<>();

    @Override
    protected void configure()
    {
        MailClientPreStartup.getPackageMailConnections().forEach((packageName, connections) -> {
            for (MailConnectionOptions options : connections)
            {
                String connectionName = options.value();
                if (mailClients.containsKey(connectionName))
                {
                    continue;
                }

                MailConfig config = toMailConfig(options);
                MailClient client = MailClient.createShared(VertXPreStartup.getVertx(), config, connectionName);
                mailClients.put(connectionName, client);

                // Bind MailClient with @Named
                bind(Key.get(MailClient.class, Names.named(connectionName))).toInstance(client);

                // Bind MailService with @Named
                String defaultFrom = options.defaultFrom();
                bind(Key.get(MailService.class, Names.named(connectionName)))
                        .toProvider(() -> new MailService(client, connectionName, defaultFrom))
                        .in(Singleton.class);

                log.info("Mail client '{}' configured: host={}:{}, ssl={}, startTls={}",
                        connectionName, options.hostname(), options.port(), options.ssl(), options.startTls());
            }
        });
    }

    /**
     * Converts annotation options to a Vert.x {@link MailConfig}.
     */
    public static MailConfig toMailConfig(MailConnectionOptions options)
    {
        MailConfig config = new MailConfig();
        config.setHostname(options.hostname());
        config.setPort(options.port());
        config.setSsl(options.ssl());
        config.setTrustAll(options.trustAll());
        config.setKeepAlive(options.keepAlive());
        config.setMaxPoolSize(options.maxPoolSize());
        config.setAllowRcptErrors(options.allowRcptErrors());
        config.setDisableEsmtp(options.disableEsmtp());
        config.setPipelining(options.pipelining());

        // StartTLS
        switch (options.startTls())
        {
            case DISABLED -> config.setStarttls(StartTLSOptions.DISABLED);
            case OPTIONAL -> config.setStarttls(StartTLSOptions.OPTIONAL);
            case REQUIRED -> config.setStarttls(StartTLSOptions.REQUIRED);
        }

        // Login
        switch (options.login())
        {
            case DISABLED -> config.setLogin(LoginOption.DISABLED);
            case NONE -> config.setLogin(LoginOption.NONE);
            case REQUIRED -> config.setLogin(LoginOption.REQUIRED);
        }

        // Credentials
        if (!options.username().isBlank())
        {
            config.setUsername(options.username());
        }
        if (!options.password().isBlank())
        {
            config.setPassword(options.password());
        }

        // Optional settings
        if (!options.ehloHostname().isBlank())
        {
            config.setOwnHostname(options.ehloHostname());
        }
        if (!options.authMethods().isBlank())
        {
            config.setAuthMethods(options.authMethods());
        }
        if (!options.userAgent().isBlank())
        {
            config.setUserAgent(options.userAgent());
        }
        if (options.maxMailsPerConnection() > 0)
        {
            config.setMaxMailsPerConnection(options.maxMailsPerConnection());
        }
        if (options.keepAliveTimeout() != 300)
        {
            config.setKeepAliveTimeout(options.keepAliveTimeout());
        }

        config.setEnableDKIM(options.enableDkim());

        return config;
    }
}


