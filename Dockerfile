FROM greboid/kotlin
WORKDIR /app
COPY . /app
RUN /entrypoint.sh
ENTRYPOINT [""]
EXPOSE 80
CMD ["java","-jar","build/libs/app.jar"]
