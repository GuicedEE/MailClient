package com.guicedee.mailclient.implementations;

import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Post-startup hook for the mail client. Currently a no-op placeholder
 * for potential future initialization (e.g. health checks, connection validation).
 */
@Log4j2
public class MailClientPostStartup implements IGuicePostStartup<MailClientPostStartup>
{
    @Override
    public List<Uni<Boolean>> postLoad()
    {
        return List.of(Uni.createFrom().item(() -> {
            log.info("Mail client initialized. {} connection(s) configured.",
                    MailClientModule.getMailClients().size());
            return true;
        }));
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MAX_VALUE - 200;
    }
}

