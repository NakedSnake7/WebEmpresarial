package com.webempresarial.store.feature.event;

import java.time.LocalDateTime;
import java.util.Map;

import com.webempresarial.store.feature.runtime.ExecutionContext;

public record PlatformEvent(
        String name,
        String sourceModule,
        ExecutionContext executionContext,
        Object payload,
        Map<String, Object> metadata,
        LocalDateTime occurredAt
) {

	public static PlatformEvent of(
	        String name,
	        String sourceModule,
	        Object payload
	) {
	    return new PlatformEvent(
	            name,
	            sourceModule,
	            new ExecutionContext(),
	            payload,
	            Map.of(),
	            LocalDateTime.now()
	    );
	}

	public static PlatformEvent of(
	        String name,
	        String sourceModule,
	        Object payload,
	        Map<String, Object> metadata
	) {
	    return new PlatformEvent(
	            name,
	            sourceModule,
	            new ExecutionContext(),
	            payload,
	            metadata != null ? metadata : Map.of(),
	            LocalDateTime.now()
	    );
	}

	public static PlatformEvent of(
	        String name,
	        String sourceModule,
	        ExecutionContext executionContext,
	        Object payload,
	        Map<String, Object> metadata
	) {
	    return new PlatformEvent(
	            name,
	            sourceModule,
	            executionContext != null ? executionContext : new ExecutionContext(),
	            payload,
	            metadata != null ? metadata : Map.of(),
	            LocalDateTime.now()
	    );
	}
}