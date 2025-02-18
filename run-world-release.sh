#!/bin/bash

while :
do
  git pull origin master
  GITLAB_API_URL="https://gitlab.com/api/v4"
  PROJECT_ID="42380105"

  PACKAGE_DOWNLOAD_URL=$(curl -s --header "PRIVATE-TOKEN: ${GITLAB_ACCESS_TOKEN}" "${GITLAB_API_URL}/projects/${PROJECT_ID}/releases?order_by=created_at&sort=desc" | jq -r 'max_by(.released_at) | .assets.links | map(select(.name | endswith("-jar"))) | .[0].direct_asset_url')

  curl -L --header "PRIVATE-TOKEN: ${GITLAB_ACCESS_TOKEN}" "${PACKAGE_DOWNLOAD_URL}" > lobby-server.jar

  echo "Successfully downloaded the latest package release (${PACKAGE_NAME})"
  java --enable-preview $DARKAN_JAVA_VM_ARGS -jar lobby-server.jar >> mainlog.txt

  echo "You have 1 seconds to stop the server using Ctrl-C. Server will restart otherwise"
  sleep 1
done
