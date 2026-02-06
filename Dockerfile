FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.01.29.1157z

COPY build/libs/*.jar app.jar

CMD ["-jar", "app.jar"]
