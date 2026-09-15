#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ -f .env ]; then
  set -a
  . ./.env
  set +a
fi
exec java -jar release/super-ace-online-13.0.0.jar
