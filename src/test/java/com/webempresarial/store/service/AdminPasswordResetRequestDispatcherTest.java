package com.webempresarial.store.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminPasswordResetRequestDispatcherTest {

    @Test
    void requestIsNotProcessedUntilWorkerRuns() {

        AdminPasswordResetService service =
                mock(AdminPasswordResetService.class);

        Queue<Runnable> tasks = new ArrayDeque<>();
        Executor executor = tasks::add;

        var dispatcher =
                new AdminPasswordResetRequestDispatcher(
                        service, executor
                );

        dispatcher.dispatch("admin@example.invalid");

        assertEquals(1, tasks.size());
        verifyNoInteractions(service);

        tasks.remove().run();

        verify(service).requestRecovery(
                "admin@example.invalid"
        );
    }

    @Test
    void invalidInputDoesNotEnterQueue() {

        AdminPasswordResetService service =
                mock(AdminPasswordResetService.class);

        Executor executor = mock(Executor.class);

        var dispatcher =
                new AdminPasswordResetRequestDispatcher(
                        service, executor
                );

        dispatcher.dispatch(null);
        dispatcher.dispatch(" ");
        dispatcher.dispatch("x".repeat(255));

        verifyNoInteractions(executor, service);
    }

    @Test
    void queueRejectionIsHandled() {

        AdminPasswordResetService service =
                mock(AdminPasswordResetService.class);

        Executor executor = task -> {
            throw new RejectedExecutionException(
                    "Simulated capacity limit"
            );
        };

        var dispatcher =
                new AdminPasswordResetRequestDispatcher(
                        service, executor
                );

        assertDoesNotThrow(
                () -> dispatcher.dispatch(
                        "admin@example.invalid"
                )
        );

        verifyNoInteractions(service);
    }

    @Test
    void workerFailureDoesNotEscape() {

        AdminPasswordResetService service =
                mock(AdminPasswordResetService.class);

        Queue<Runnable> tasks = new ArrayDeque<>();

        var dispatcher =
                new AdminPasswordResetRequestDispatcher(
                        service,
                        tasks::add
                );

        doThrow(new IllegalStateException(
                "Simulated processing error"
        ))
        .when(service)
        .requestRecovery("admin@example.invalid");

        dispatcher.dispatch("admin@example.invalid");

        assertDoesNotThrow(
                () -> tasks.remove().run()
        );
    }
}
