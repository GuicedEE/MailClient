import com.guicedee.client.services.lifecycle.IGuiceModule;
import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.guicedee.client.services.lifecycle.IGuicePreDestroy;
import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import com.guicedee.mailclient.implementations.*;

module com.guicedee.mailclient {
    exports com.guicedee.mailclient;
    exports com.guicedee.mailclient.implementations;

    requires transitive io.vertx.mail.client;
    requires com.guicedee.vertx;
    requires com.guicedee.client;
    requires static lombok;

    requires io.github.classgraph;
    requires org.apache.commons.lang3;

    provides IGuicePostStartup with MailClientPostStartup;
    provides IGuiceModule with MailClientModule;
    provides IGuicePreStartup with MailClientPreStartup;
    provides IGuicePreDestroy with MailClientPreDestroy;

    opens com.guicedee.mailclient to com.google.guice, com.fasterxml.jackson.databind;
    opens com.guicedee.mailclient.implementations to com.fasterxml.jackson.databind, com.google.guice;
}


