# build
FROM node:23 AS builder

RUN mkdir /opt/app && chown -R node:node /opt/app #make node the owner

USER node

WORKDIR /opt/app

#make node the owner
COPY --chown=node:node package*.json ./

RUN npm ci --ignore-scripts

#make node the owner and make scripts executable, otherwise a permission denied error occurs
COPY --chown=node:node --chmod=755 . .

RUN npm run build


# serve frontend with nginx
FROM nginx:stable-alpine

# Default values for environment variables
ENV BACKEND_HOST="http://test"
ENV BACKEND_PORT="8080"
ENV BACKEND_PROTO="http"

# Copy the build files from the previous stage
COPY --from=builder /opt/app/dist /usr/share/nginx/html

# Copy nginx site-configchat
COPY nginx-site.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Create config.json file from environment variables and start nginx
CMD ["/bin/sh", "-c", "echo \"{\\\"BACKEND_HOST\\\": \\\"${BACKEND_HOST}\\\", \\\"BACKEND_PORT\\\": \\\"${BACKEND_PORT}\\\", \\\"BACKEND_PROTO\\\":\\\"${BACKEND_PROTO}\\\"\"} > /usr/share/nginx/html/config.json && cat /usr/share/nginx/html/config.json && cat /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
