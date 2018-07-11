FROM python:3.7-alpine

RUN apk --no-cache add \
                       python-dev \
                       build-base \
                       jpeg-dev \
                       zlib-dev \
                       freetype-dev \
                       lcms2-dev \
                       openjpeg-dev \
                       tiff-dev \
                       tk-dev \
                       tcl-dev \
                       harfbuzz-dev \
                       fribidi-dev

WORKDIR /app
RUN touch /app/database.sqlite

COPY requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

RUN pip install gunicorn eventlet

RUN \
  addgroup -g 9999 -S pythonuser && \
  adduser -S -G pythonuser -u 9999 -s /bin/bash -h /app pythonuser

USER pythonuser

COPY feed.py /app/feed.py

ENTRYPOINT ["gunicorn", "--forwarded-allow-ips", "0.0.0.0/0", "-k", "eventlet", "-b", "0.0.0.0:8000", "feed:app"]
