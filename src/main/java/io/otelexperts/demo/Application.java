package io.otelexperts.demo;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;


public class Application {

    public static void main(String[] args) {
        ApplicationContext appContext = Micronaut.run(Application.class, args);
        //
        OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();

        // Gets or creates a named tracer instance
        Tracer tracer = openTelemetry
                .getTracerProvider()
                .tracerBuilder("app-tracer")
                .build();

        // auto instrumentation does not create a global meter provider unlike tracerprovider, so need to init a new one
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
                //.setResource(resource)
                .build();
        // Gets or creates a named meter instance
        Meter meter = sdkMeterProvider
                .meterBuilder("app-metrics")
                .setInstrumentationVersion("1.0.0")
                .build();


        appContext.registerSingleton(OpenTelemetry.class, openTelemetry);
        appContext.registerSingleton(Tracer.class, tracer);
        appContext.registerSingleton(Meter.class, meter);
    }
}