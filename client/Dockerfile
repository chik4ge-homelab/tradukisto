# syntax=docker/dockerfile:1

FROM oven/bun:1.3.5 AS build
WORKDIR /app

ARG VITE_BEARER_TOKEN
ARG VITE_API_BASE_URL

COPY client/package.json client/bun.lock ./
RUN bun install --frozen-lockfile

COPY client/ ./
ENV VITE_BEARER_TOKEN=${VITE_BEARER_TOKEN}
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN bun run build

FROM nginx:1.27-alpine
RUN apk add --no-cache curl
COPY client/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
