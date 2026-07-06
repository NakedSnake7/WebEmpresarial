package com.webempresarial.store.feature.event;

import com.webempresarial.store.feature.runtime.TraceType; 
import com.webempresarial.store.feature.runtime.annotations.Trace;
import com.webempresarial.store.service.PlatformEventHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformEventBus {

    private static final Logger log =
            LoggerFactory.getLogger(PlatformEventBus.class);

    private final List<PlatformEventListener> listeners;
    private final PlatformEventHistoryService historyService;

    public PlatformEventBus(
            List<PlatformEventListener> listeners,
            PlatformEventHistoryService historyService
    ) {
        this.listeners = listeners;
        this.historyService = historyService;
    }

    @Trace(
            type = TraceType.EVENT,name = "PlatformEventBus.publish", source = "PlatformEventBus")
    public PlatformEventReport publish(PlatformEvent event) {
        PlatformEventReport report = new PlatformEventReport(event);

        listeners.stream()
                .filter(listener -> listener.supports(event.name()))
                .forEach(listener -> executeListener(listener, event, report));

        historyService.save(report);

        return report;
    }

    private void executeListener(
            PlatformEventListener listener,
            PlatformEvent event,
            PlatformEventReport report
    ) {
        long start = System.currentTimeMillis();

        try {
            listener.handle(event);

            report.add(new EventExecutionResult(
                    listener.getClass().getSimpleName(),
                    true,
                    System.currentTimeMillis() - start,
                    "OK"
            ));

        } catch (Exception ex) {
            report.add(new EventExecutionResult(
                    listener.getClass().getSimpleName(),
                    false,
                    System.currentTimeMillis() - start,
                    ex.getMessage()
            ));

            log.error(
                    "Error ejecutando listener {} para evento {}",
                    listener.getClass().getSimpleName(),
                    event.name(),
                    ex
            );
        }
    }
}