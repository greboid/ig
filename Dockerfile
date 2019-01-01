FROM greboid/kotlin
WORKDIR /app
COPY /app /app
RUN /entrypoint.sh
ENTRYPOINT [""]
CMD ["java","-jar","build/libs/app.jar"]