#!/bin/bash

GITLAB_API_URL="https://gitlab.com/api/v4"
PROJECT_ID="42380105"
PROJECT_NAME="lobby-server"

get_latest_version() {
  curl --header "PRIVATE-TOKEN: $GITLAB_ACCESS_TOKEN" \
    "$GITLAB_API_URL/projects/$PROJECT_ID/packages/maven/rs/darkan/$PROJECT_NAME/maven-metadata.xml" \
    | xmllint --xpath 'string(/metadata/versioning/latest)' -
}

while :
do
  git pull origin dev

  PACKAGE_VERSION=$(get_latest_version)

  DOWNLOAD_URL="${GITLAB_API_URL}/projects/${PROJECT_ID}/packages/maven/rs/darkan/${PROJECT_NAME}/${PACKAGE_VERSION}/${PROJECT_NAME}-${PACKAGE_VERSION}.jar"
  echo "Downloading package: ${DOWNLOAD_URL}"

  curl -L --header "PRIVATE-TOKEN: ${GITLAB_ACCESS_TOKEN}" "${DOWNLOAD_URL}" > lobby-server.jar

  echo "Successfully downloaded the latest package release (${PROJECT_NAME}/${PACKAGE_VERSION})"
  java --enable-preview $DARKAN_JAVA_VM_ARGS -jar lobby-server.jar >> mainlog.txt

  echo "You have 1 seconds to stop the server using Ctrl-C. Server will restart otherwise"
  sleep 1
done