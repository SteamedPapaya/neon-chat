# OpenJDK 17을 기반으로 한 도커 이미지를 사용
FROM openjdk:17-jdk-slim

# JAR 파일을 컨테이너 내부로 복사
VOLUME /tmp
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 시 JAR 파일을 실행하는 명령어 지정
ENTRYPOINT ["java","-jar","/app.jar"]