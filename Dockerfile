FROM openjdk:17-jdk-alpine
COPY build/libs/leaderboard-1.0.0-SNAPSHOT-all.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
