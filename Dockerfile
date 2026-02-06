FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.02.06.0908Z

COPY build/libs/*.jar app.jar

CMD ["-jar", "app.jar"]
