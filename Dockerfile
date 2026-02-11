FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.02.11.1141Z

COPY build/libs/*.jar app.jar

CMD ["-jar", "app.jar"]
