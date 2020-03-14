FROM node:13 as react
WORKDIR /app
COPY front front
WORKDIR /app/front
ENV GENERATE_SOURCEMAP false
RUN yarn install --production && yarn build

FROM debian:10 as build
WORKDIR /app
ENV PATH="/opt/kotlin/kotlinc/bin:${PATH}"
RUN apt update && apt install -y gnupg2 software-properties-common
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9 && \
    apt-add-repository 'deb http://repos.azulsystems.com/debian stable main' && apt update && \
    apt install -y curl unzip zip zulu-13
COPY . /app
COPY --from=react /app/front/build src/main/resources/admin
RUN ./gradlew jar

FROM openjdk:13
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
ENTRYPOINT [""]
EXPOSE 80
CMD ["java","-jar","app.jar"]
