FROM hseeberger/scala-sbt:8u302_1.5.5_2.13.6

WORKDIR /app

COPY . /app

RUN sbt update

CMD ["sbt", "run"]