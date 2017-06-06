FROM java:8

ENV VERSION 1.1.2

ENV KOTLIN_COMPILER_URL https://github.com/JetBrains/kotlin/releases/download/v${VERSION}/kotlin-compiler-${VERSION}.zip

RUN wget $KOTLIN_COMPILER_URL -O /tmp/a.zip && unzip /tmp/a.zip -d /opt && rm /tmp/a.zip
#!/bin/bash

ENV PATH $PATH:/opt/kotlinc/bin

RUN git clone https://github.com/rasantos24/LP_Kotlin

CMD cd /LP_Kotlin && ./gradlew run

EXPOSE 8080
