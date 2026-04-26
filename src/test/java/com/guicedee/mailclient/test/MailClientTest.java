package com.guicedee.mailclient.test;

import com.guicedee.client.IGuiceContext;
import com.guicedee.mailclient.MailService;
import com.google.inject.Key;
import com.google.inject.name.Names;
import io.vertx.ext.mail.MailClient;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GuicedEE Mail Client module.
 * <p>
 * These tests verify annotation scanning, Guice binding, and MailService injection.
 * A real SMTP server is NOT required — tests validate the wiring only.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MailClientTest
{
    @BeforeAll
    static void init()
    {
        IGuiceContext.registerModule("com.guicedee.mailclient.test");
        IGuiceContext.instance().inject();
    }

    @AfterAll
    static void destroy()
    {
        IGuiceContext.instance().destroy();
    }

    @Test
    @Order(1)
    void testMailClientBound()
    {
        var client = IGuiceContext.get(Key.get(MailClient.class, Names.named("test-smtp")));
        assertNotNull(client, "MailClient should be bound with @Named('test-smtp')");
    }

    @Test
    @Order(2)
    void testMailServiceBound()
    {
        var service = IGuiceContext.get(Key.get(MailService.class, Names.named("test-smtp")));
        assertNotNull(service, "MailService should be bound with @Named('test-smtp')");
        assertEquals("test-smtp", service.getConnectionName());
        assertEquals("test@example.com", service.getDefaultFrom());
    }

    @Test
    @Order(3)
    void testMailServiceSingleton()
    {
        var service1 = IGuiceContext.get(Key.get(MailService.class, Names.named("test-smtp")));
        var service2 = IGuiceContext.get(Key.get(MailService.class, Names.named("test-smtp")));
        assertSame(service1, service2, "MailService should be a singleton");
    }
}

