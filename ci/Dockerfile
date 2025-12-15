# ci/Dockerfile
FROM maven:3.9.11-eclipse-temurin-21

ARG NODE_MAJOR=20
ARG YQ_VERSION=v4.44.6
ARG GH_VERSION=2.56.0

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      ca-certificates curl gnupg git bash \
 && rm -rf /var/lib/apt/lists/*

# Node.js (LTS by default)
RUN apt-get update \
 && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /usr/share/keyrings/nodesource.gpg \
 && echo "deb [signed-by=/usr/share/keyrings/nodesource.gpg] https://deb.nodesource.com/node_${NODE_MAJOR}.x nodistro main" \
    > /etc/apt/sources.list.d/nodesource.list \
 && apt-get install -y --no-install-recommends nodejs \
 && node -v && npm -v \
 && rm -rf /var/lib/apt/lists/*

# yq (Mike Farah) - YAML editing
RUN curl -fsSL -o /usr/local/bin/yq \
      "https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/yq_linux_amd64" \
 && chmod +x /usr/local/bin/yq \
 && yq --version

# GitHub CLI (gh) - PR creation
RUN curl -fsSL -o /tmp/gh.tgz \
      "https://github.com/cli/cli/releases/download/v${GH_VERSION}/gh_${GH_VERSION}_linux_amd64.tar.gz" \
 && tar -xzf /tmp/gh.tgz -C /tmp \
 && mv "/tmp/gh_${GH_VERSION}_linux_amd64/bin/gh" /usr/local/bin/gh \
 && chmod +x /usr/local/bin/gh \
 && gh --version \
 && rm -rf /tmp/gh.tgz "/tmp/gh_${GH_VERSION}_linux_amd64"

WORKDIR /workspace