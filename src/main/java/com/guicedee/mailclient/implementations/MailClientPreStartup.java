package com.guicedee.mailclient.implementations;

import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import com.guicedee.mailclient.MailConnectionOptions;
import com.guicedee.vertx.spi.VertXPreStartup;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Pre-startup scanner that discovers {@link MailConnectionOptions} annotations
 * and registers connection metadata for later binding.
 */
@Log4j2
public class MailClientPreStartup implements IGuicePreStartup<MailClientPreStartup>
{
    /**
     * Package-level mail connection options keyed by package name.
     */
    @Getter
    private static final Map<String, List<MailConnectionOptions>> packageMailConnections = new TreeMap<>();

    /**
     * Maps connection name to the connection options.
     */
    @Getter
    private static final Map<String, MailConnectionOptions> connectionsByName = new HashMap<>();

    @Override
    public List<Future<Boolean>> onStartup()
    {
        return List.of(VertXPreStartup.getVertx().executeBlocking(() -> {
            ScanResult scanResult = IGuiceContext.instance().getScanResult();
            processConnections(scanResult);
            processPackageConnections(scanResult);
            return true;
        }));
    }

    private void processConnections(ScanResult scanResult)
    {
        ClassInfoList connectionClasses = scanResult.getClassesWithAnnotation(MailConnectionOptions.class);
        connectionClasses.stream()
                .distinct()
                .forEach(ci -> {
                    log.debug("Found Mail Connection on class - {}", ci.getName());
                    var ann = ci.loadClass().getAnnotation(MailConnectionOptions.class);
                    String connectionName = ann.value();
                    MailConnectionOptions wrapped = wrapConnectionOptions(connectionName, ann);
                    packageMailConnections.computeIfAbsent(ci.getPackageName(), k -> new ArrayList<>()).add(wrapped);
                    connectionsByName.putIfAbsent(connectionName, wrapped);
                });
    }

    private void processPackageConnections(ScanResult scanResult)
    {
        var packageInfoList = scanResult.getPackageInfo();
        if (packageInfoList != null)
        {
            packageInfoList.forEach(packageInfo -> {
                try
                {
                    var packageName = packageInfo.getName();
                    String packageInfoClassName = packageName + ".package-info";
                    try
                    {
                        Class<?> packageInfoClass = Class.forName(packageInfoClassName);
                        var ann = packageInfoClass.getAnnotation(MailConnectionOptions.class);
                        if (ann != null && !connectionsByName.containsKey(ann.value()))
                        {
                            String connectionName = ann.value();
                            MailConnectionOptions wrapped = wrapConnectionOptions(connectionName, ann);
                            packageMailConnections.computeIfAbsent(packageName, k -> new ArrayList<>()).add(wrapped);
                            connectionsByName.putIfAbsent(connectionName, wrapped);
                            log.debug("Found Mail Connection on package-info - {} (connection={})", packageName, connectionName);
                        }
                    }
                    catch (ClassNotFoundException ignored)
                    {
                    }
                }
                catch (Exception e)
                {
                    log.trace("Error processing package info: {}", e.getMessage());
                }
            });
        }
    }

    private MailConnectionOptions wrapConnectionOptions(String connectionName, MailConnectionOptions ann)
    {
        return new MailConnectionOptions()
        {
            @Override
            public Class<? extends Annotation> annotationType() { return MailConnectionOptions.class; }

            @Override
            public String value() { return envForName(connectionName, "CONNECTION_NAME", ann.value()); }

            @Override
            public String hostname() { return envForName(connectionName, "HOSTNAME", ann.hostname()); }

            @Override
            public int port() { return Integer.parseInt(envForName(connectionName, "PORT", String.valueOf(ann.port()))); }

            @Override
            public StartTLSMode startTls() { return StartTLSMode.valueOf(envForName(connectionName, "START_TLS", ann.startTls().name())); }

            @Override
            public LoginMode login() { return LoginMode.valueOf(envForName(connectionName, "LOGIN", ann.login().name())); }

            @Override
            public String username() { return envForName(connectionName, "USERNAME", ann.username()); }

            @Override
            public String password() { return envForName(connectionName, "PASSWORD", ann.password()); }

            @Override
            public boolean ssl() { return Boolean.parseBoolean(envForName(connectionName, "SSL", String.valueOf(ann.ssl()))); }

            @Override
            public boolean trustAll() { return Boolean.parseBoolean(envForName(connectionName, "TRUST_ALL", String.valueOf(ann.trustAll()))); }

            @Override
            public String ehloHostname() { return envForName(connectionName, "EHLO_HOSTNAME", ann.ehloHostname()); }

            @Override
            public String authMethods() { return envForName(connectionName, "AUTH_METHODS", ann.authMethods()); }

            @Override
            public boolean keepAlive() { return Boolean.parseBoolean(envForName(connectionName, "KEEP_ALIVE", String.valueOf(ann.keepAlive()))); }

            @Override
            public int maxPoolSize() { return Integer.parseInt(envForName(connectionName, "MAX_POOL_SIZE", String.valueOf(ann.maxPoolSize()))); }

            @Override
            public boolean allowRcptErrors() { return Boolean.parseBoolean(envForName(connectionName, "ALLOW_RCPT_ERRORS", String.valueOf(ann.allowRcptErrors()))); }

            @Override
            public boolean disableEsmtp() { return Boolean.parseBoolean(envForName(connectionName, "DISABLE_ESMTP", String.valueOf(ann.disableEsmtp()))); }

            @Override
            public String userAgent() { return envForName(connectionName, "USER_AGENT", ann.userAgent()); }

            @Override
            public boolean enableDkim() { return Boolean.parseBoolean(envForName(connectionName, "ENABLE_DKIM", String.valueOf(ann.enableDkim()))); }

            @Override
            public boolean pipelining() { return Boolean.parseBoolean(envForName(connectionName, "PIPELINING", String.valueOf(ann.pipelining()))); }

            @Override
            public long maxMailsPerConnection() { return Long.parseLong(envForName(connectionName, "MAX_MAILS_PER_CONNECTION", String.valueOf(ann.maxMailsPerConnection()))); }

            @Override
            public int keepAliveTimeout() { return Integer.parseInt(envForName(connectionName, "KEEP_ALIVE_TIMEOUT", String.valueOf(ann.keepAliveTimeout()))); }

            @Override
            public String defaultFrom() { return envForName(connectionName, "DEFAULT_FROM", ann.defaultFrom()); }
        };
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MIN_VALUE + 80;
    }

    /**
     * Resolves an environment variable or system property scoped by connection name.
     * Lookup order:
     * 1. MAIL_{NORMALIZED_NAME}_{PROPERTY}
     * 2. MAIL_{PROPERTY}
     * 3. defaultValue
     */
    static String envForName(String name, String property, String defaultValue)
    {
        String normalizedName = name.toUpperCase().replace('-', '_').replace('.', '_');
        String scopedKey = "MAIL_" + normalizedName + "_" + property;
        String scopedValue = com.guicedee.client.Environment.getSystemPropertyOrEnvironment(scopedKey, null);
        if (scopedValue != null && !scopedValue.isBlank())
        {
            return scopedValue;
        }
        return com.guicedee.client.Environment.getSystemPropertyOrEnvironment("MAIL_" + property, defaultValue);
    }
}

