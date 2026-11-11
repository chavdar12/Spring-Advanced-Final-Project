#!/bin/sh
# One-shot: index template in Elasticsearch, then data view + usage dashboard in Kibana.
set -eu
until curl -fs http://elasticsearch:9200/_cluster/health >/dev/null; do sleep 3; done
curl -fsS -X PUT http://elasticsearch:9200/_index_template/trade-zone \
  -H 'Content-Type: application/json' --data-binary @/setup/index-template.json
echo
until curl -fs http://kibana:5601/kibana/api/status | grep -q '"level":"available"'; do sleep 5; done
curl -fsS -X POST 'http://kibana:5601/kibana/api/saved_objects/_import?overwrite=true' \
  -H 'kbn-xsrf: true' -F file=@/setup/dashboard.ndjson
echo
