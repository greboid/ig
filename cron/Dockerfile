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
RUN mkdir -p /app/db
WORKDIR /app

COPY requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

RUN \
  addgroup -g 9999 -S pythonuser && \
  adduser -S -G pythonuser -u 9999 -s /bin/bash -h /app pythonuser

COPY entrypoint.py /usr/local/bin/entrypoint.py
COPY cache.py /app/cache.py

RUN chown -R 9999:9999 /app

USER pythonuser

ENTRYPOINT ["/usr/local/bin/entrypoint.py"]
