package com.rally.inventory_service.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class TracingObservationTest {

    @Test
    @DisplayName("Verify Micrometer-OTel bridge initializes span and exposes traceId / spanId")
    void testTracerInitializesSpan() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
        io.opentelemetry.api.trace.Tracer otelTracer = openTelemetry.getTracer("test-tracer");

        OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        OtelBaggageManager baggageManager = new OtelBaggageManager(currentTraceContext, Collections.emptyList(), Collections.emptyList());
        Tracer tracer = new OtelTracer(otelTracer, currentTraceContext, event -> {}, baggageManager);

        Span span = tracer.nextSpan().name("test-span").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            assertThat(span.context().traceId()).isNotBlank();
            assertThat(span.context().spanId()).isNotBlank();
        } finally {
            span.end();
        }
    }
}
