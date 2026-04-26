package com.guicedee.mailclient.implementations;

import com.guicedee.client.services.lifecycle.IGuicePreDestroy;
import lombok.extern.log4j.Log4j2;

/**
 * Cleans up Vert.x mail clients on application shutdown.
 */
@Log4j2
public class MailClientPreDestroy implements IGuicePreDestroy<MailClientPreDestroy>
{
    @Override
    public void onDestroy()
    {
        log.info("Shutting down mail clients...");

        MailClientModule.getMailClients().forEach((connectionName, client) -> {
            try
            {
                client.close()
                        .onSuccess(v -> log.debug("Mail client '{}' closed.", connectionName))
                        .onFailure(t -> log.error("Failed to close mail client '{}'", connectionName, t));
            }
            catch (Exception e)
            {
                log.error("Error closing mail client '{}'", connectionName, e);
            }
        });

        log.info("Mail client shutdown complete.");
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MAX_VALUE - 100;
    }
}

