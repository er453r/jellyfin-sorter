#!/usr/bin/env bash

IMAGE="jellyfin-sorter"
BRANCH=$(git rev-parse --abbrev-ref HEAD)
TAG=$(git tag --points-at HEAD | head -n1)

echo -e "\nIMAGE\t$IMAGE"
echo -e  "BRANCH\t$BRANCH"
echo -e "TAG\t$TAG\n"

REPO="ci.kolektiv.dev"

DOCKER_TAG="$IMAGE:$BRANCH"

if [ ! -z "${TAG}" ]; then
  echo -e "Building tags $DOCKER_TAG, $IMAGE:$TAG...\n"

  docker buildx build -t "$REPO/$DOCKER_TAG" -t "$REPO/$IMAGE:$TAG" .
  docker push "$REPO/$DOCKER_TAG"
  docker push "$REPO/$IMAGE:$TAG"
else
  echo -e "Building tag $DOCKER_TAG...\n"

  docker buildx build -t "$REPO/$DOCKER_TAG" .
  docker push "$REPO/$DOCKER_TAG"
fi

echo -e "\nBuilding tag $DOCKER_TAG - DONE!"
