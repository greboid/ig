FROM python:3.7-rc-alpine

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

COPY requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

RUN \
  addgroup -g 9999 -S pythonuser && \
  adduser -S -G pythonuser -u 9999 -s /bin/bash -h /app pythonuser

USER pythonuser

COPY entrypoint.py /usr/local/bin/entrypoint.py
COPY cache.py /app/cache.py

ENTRYPOINT ["/usr/local/bin/entrypoint.py"]
