#!/bin/bash

set -eo pipefail

VERSION=${TRAVIS_BRANCH#"release/"}

export PREVIOUS_RELEASE=$(curl -H "Authorization: token ${GITHUB_PERSONAL_ACCESS_TOKEN}" -s https://api.github.com/repos/web3j/web3j/releases/latest | jq -r '.target_commitish' )
export CHANGELOG=$(git rev-list --format=oneline --abbrev-commit --max-count=50 ${PREVIOUS_RELEASE}..HEAD | jq --slurp --raw-input . )

echo "Creating a new release on GitHub"
API_JSON="{
  \"tag_name\": \"v${VERSION}\",
  \"target_commitish\": \"$(git rev-parse HEAD)\",
  \"name\": \"v${VERSION}\",
  \"body\": \"Release of version ${VERSION}: \n\n ${CHANGELOG:1:-1}\",
  \"draft\": false,
  \"prerelease\": false
}"


for FILE in `find ./build/distributions -type f -name "web3j.*"`;
do
  curl -H "Authorization: token ${GITHUB_PERSONAL_ACCESS_TOKEN}" -s "${UPLOAD_URL:0:-13}?name=$(basename -- $FILE)" -H "Content-Type: $(file -b --mime-type $FILE)" --data-binary @"${FILE}"
done
