FROM nimmis/java:openjdk-8-jdk
#RUN export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)" && \
#
#echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list && \
#curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add - && \
#sudo apt-get update && sudo apt-get install google-cloud-sdk && \
#gcloud init

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
