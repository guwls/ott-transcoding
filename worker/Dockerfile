FROM eclipse-temurin:21-jre
WORKDIR /app
# 스냅샷/릴리스 둘 다 잡히게
COPY worker/build/libs/*.jar /app/worker.jar
ENTRYPOINT ["java","-jar","/app/worker.jar"]
