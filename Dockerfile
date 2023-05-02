FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.19_7
RUN mkdir /app
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
COPY --chown=javauser:javauser ./build/libs/payment-service-0.1-all.jar /app/payment-service-0.1.jar
ADD --chown=javauser:javauser https://github.com/signalfx/splunk-otel-java/releases/latest/download/splunk-otel-javaagent.jar /opt/splunk-otel-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/opt/splunk-otel-javaagent.jar
WORKDIR /app
RUN chown -R javauser:javauser /app
USER javauser
CMD "java" "-Dsplunk.metrics.enabled=true" "-jar" "payment-service-0.1.jar"
