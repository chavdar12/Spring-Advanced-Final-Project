# build context: repository root. The gateway serves the Angular build and fronts the API.
FROM node:24-alpine AS client
WORKDIR /client
COPY client/package.json client/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY client/ .
RUN npx ng build

FROM nginx:1.29-alpine
COPY gateway/nginx.conf /etc/nginx/conf.d/default.conf
COPY gateway/kibana-htpasswd.sh /docker-entrypoint.d/40-kibana-htpasswd.sh
COPY --from=client /client/dist/client/browser /usr/share/nginx/html
HEALTHCHECK --interval=10s --timeout=3s CMD wget -qO- http://127.0.0.1/healthz || exit 1
