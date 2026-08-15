FROM mcr.microsoft.com/playwright/java:v1.48.0-noble

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

CMD ["mvn", "-B", "test", "-Dheadless=true", "-Dbrowser=chromium"]
