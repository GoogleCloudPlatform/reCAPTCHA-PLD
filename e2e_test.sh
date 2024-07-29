#!/bin/bash

# Copyright 2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

readonly TEST_IMAGE="test-pld-img"
readonly TEST_CONTAINER_NAME="test-pld"
readonly RECAPTCHA_PROJECT_ID=$1
readonly GOOGLE_CLOUD_API_KEY=$2
readonly CREATE_ASSESSMENT_URL='0.0.0.0:8080/createAssessment'

function start_test_env() {
  echo "Building test image ${TEST_IMAGE}"
  docker build . -t "${TEST_IMAGE}"
  echo "Starting test container ${TEST_CONTAINER_NAME}"
  docker run -d \
    --network host \
    -e RECAPTCHA_PROJECT_ID="${RECAPTCHA_PROJECT_ID}" \
    -e GOOGLE_CLOUD_API_KEY="${GOOGLE_CLOUD_API_KEY}" \
    --name="${TEST_CONTAINER_NAME}" \
    "${TEST_IMAGE}"
}

function stop_test_env() {
  echo "Stopping test container ${TEST_CONTAINER_NAME}"
  docker stop "${TEST_CONTAINER_NAME}"
  echo "Removing test container ${TEST_CONTAINER_NAME}"
  docker rm "${TEST_CONTAINER_NAME}"
  echo "Removing local test image ${TEST_IMAGE}"
  docker rmi "${TEST_IMAGE}"
}

function wait_until_healthy() {
  local total_sleep_time=$1
  while [[ "$(docker inspect -f {{.State.Running}} $TEST_CONTAINER_NAME)" != "true" ]] || \
  [[ "$(curl -s --write-out '%{response_code}' -o /dev/null 0.0.0.0:8080)" = 000 ]]; do
    if [[ "${sleep_time}" -gt $total_sleep_time ]] then
      echo "Failed: Container or server not running after ${total_sleep_time}s"
      break
    fi
    sleep 1
    ((sleep_time++))
    echo "Waiting till healthy..."
  done
}

function test_random_credentials() {
  local random_password=$(xxd -l24 -ps /dev/urandom | xxd -r -ps | base64)
  local resp=$(curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"leakedusername","password":"'"$random_password"'"}' \
  "${CREATE_ASSESSMENT_URL}" -sS -w " %{response_code}" || true)
  assert_equal "${resp}" 'NO_STATUS' 'test_random_credentials'
}

function test_leaked_credentials() {
  local resp=$(curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"leakedusername","password":"leakedpassword"}' \
  "${CREATE_ASSESSMENT_URL}" -sS -w " %{response_code}" || true)
  assert_equal "${resp}" 'LEAKED' 'test_leaked_credentials'
}

function assert_equal() {
  local resp=$1
  local expected_resp=$2
  local test_name=$3
  local status_code="${resp##* }"
  local content=$(jq -r '.leakedStatus' <<< ${resp% *})
  if [[ $status_code != "200" ]] || [[ -z $content ]]; then
    echo "$test_name failed with $status_code"
  elif [[ $content != $expected_resp ]] then
    echo "$test_name failed, actual: $content, expected: $expected_resp"
  else
    echo "$test_name passed"
  fi
}

start_test_env
wait_until_healthy 10
echo "----------------------------------------"
test_leaked_credentials
test_random_credentials
echo "----------------------------------------"
stop_test_env
