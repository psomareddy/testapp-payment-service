package io.otelexperts.demo.controller;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.otelexperts.demo.common.Simulator;
import io.otelexperts.demo.data.OrderDetails;
import jakarta.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Controller
public class PaymentController {

    private final DoubleCounter revenueCounter;
    private final Attributes revenueCounterAttributes;
    @Inject
    private Simulator simulator;
    private final Meter meter;
    private final Tracer tracer;

    public PaymentController(Tracer tracer, Meter meter) {
        this.tracer = tracer;
        this.meter = meter;
        revenueCounter = meter
                .counterBuilder("order_amount")
                .ofDoubles()
                .setDescription("Order Amount")
                .setUnit("1")
                .build();
        revenueCounterAttributes = Attributes.of(stringKey("CustomerId"), "Cf");
    }

    @WithSpan
    @Post("/complete-purchase/review-pay")
    public HttpResponse<?> reviewPay(@Body OrderDetails orderDetails, HttpHeaders headers) {
        logContextForAnalytics();
        createBusinessMetrics(orderDetails.getAmount());
        simulator.generateLatency(300, 399);
        return HttpResponse.ok("{'status': 'ok'}");
    }

    @WithSpan
    @Post("/complete-purchase/submit-pay")
    public HttpResponse<?> submitPayment(@Body OrderDetails orderDetails, HttpHeaders headers) throws Exception {
        debitPaymentAmount();
        createBusinessMetrics(orderDetails.getAmount());
        logContextForAnalytics();
        simulator.generateLatency(300, 399);
        return HttpResponse.ok("{'status': 'ok'}");
    }

    private void createBusinessMetrics(double orderAmount) {
        System.out.println("order amount is " + orderAmount);
        revenueCounter.add(orderAmount, revenueCounterAttributes);
    }

    private void debitPaymentAmount() throws MalformedURLException {
        URL url = new URL("http://squarepay.com:8011/debitPayment");
        Span span = tracer.spanBuilder("/submitPayment").setSpanKind(SpanKind.CLIENT).startSpan();
        span.setAttribute("http.method", "GET");
        span.setAttribute("http.url", url.toString());
        try (Scope scope = span.makeCurrent()) {
            simulator.generateLatency(150, 400);
        } finally {
            span.end();
        }
    }

    private void logContextForAnalytics() {
        /*
        // upstream root service would have added the correlationId to baggage
            Baggage.current()
              .toBuilder()
              .put("correlation.id, correlationId)
              .build()
              .makeCurrent();
         */

        String correlationId = Baggage.current().getEntryValue("correlation.id");
        Span currentSpan = Span.current();
        currentSpan.setAttribute("pay.cor.id", correlationId );
        StringBuffer logEvent = new StringBuffer();
        logEvent.append("Trace Id: ").append(currentSpan.getSpanContext().getTraceId())
                .append("Span Id: ").append(currentSpan.getSpanContext().getSpanId())
                .append("Trace State: ").append(currentSpan.getSpanContext().getTraceState())
                .append(", ").append("Correlation Id: ").append(correlationId)
                .append(", ").append("Version: ").append("1.0.0");
        System.out.println(logEvent.toString());
    }

}
