FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.03.04.0913Z

COPY build/libs/*.jar app.jar

CMD ["-jar", "app.jar"]
