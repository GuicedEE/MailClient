module com.guicedee.mailclient.test {
    requires com.guicedee.guicedinjection;
    requires com.guicedee.mailclient;
    requires com.guicedee.vertx;
    requires org.junit.jupiter.api;

    opens com.guicedee.mailclient.test to com.google.guice, com.guicedee.mailclient, org.junit.platform.commons;
}


