#!/bin/sh

echo "Updating..."

DELAY_ARG=""
if [ -z $NVD_API_KEY ]; then
  DELAY_ARG="--delay=10000"
else
  echo "Using NVD API KEY: ${NVD_API_KEY:0:5}****"
fi

if [ -n "${DELAY}" ]; then
  echo "Overriding delay with ${DELAY}ms"
  DELAY_ARG="--delay=$DELAY"
fi

if [ -n "${MAX_RETRY}" ]; then
  echo "Using max retry attempts: $MAX_RECORDS_PER_PAGE"
  MAX_RETRY_ARG="--maxRetry=$MAX_RETRY"
fi

if [ -n "${MAX_RECORDS_PER_PAGE}" ]; then
  echo "Using max records per page: $MAX_RECORDS_PER_PAGE"
  MAX_RECORDS_PER_PAGE_ARG="--recordsPerPage=$MAX_RECORDS_PER_PAGE"
fi

if [ -n "${DEBUG}" ]; then
  echo "Enabling debug mode"
  DEBUG_ARG="--debug"
fi

java $JAVA_OPT -jar /usr/local/bin/vulnz cve $DELAY_ARG $DEBUG_ARG $MAX_RETRY_ARG $MAX_RECORDS_PER_PAGE_ARG --cache --directory /usr/local/apache2/htdocs
