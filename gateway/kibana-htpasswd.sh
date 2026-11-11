#!/bin/sh
# Basic-auth user for /kibana, from KIBANA_USER / KIBANA_PASSWORD (run by the nginx entrypoint).
set -eu
printf '%s:{PLAIN}%s\n' "${KIBANA_USER:-admin}" "$KIBANA_PASSWORD" > /etc/nginx/kibana.htpasswd
# readable by the nginx worker user only
chown root:nginx /etc/nginx/kibana.htpasswd
chmod 640 /etc/nginx/kibana.htpasswd
