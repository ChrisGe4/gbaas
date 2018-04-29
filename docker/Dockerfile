FROM gcr.io/google-appengine/java-compat
ARG JAR_FILE
COPY fabric-deploy-rest-0.1.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
