#!/bin/bash
# A script to test the SSH Launcher using the CLI
# This only tests that the configuration passed to SSH is "correct" enough to make
# a request
set -o errexit
set -o nounset

# -- Base setup
ROOT_DIR="$(dirname "$0")"
LATEST_JAR="${ROOT_DIR}/build/libs/ssh-launcher-all.jar"
LAUNCH_CMD="java -jar ${LATEST_JAR}"

echo "Running tests with: ${LATEST_JAR}"


# -- Setup constants
TEST_DIR=$(mktemp -d /tmp/foo.XXXXXXXXX)
TEST_FILE="${TEST_DIR}/test-file"
TEST_HOST_KEY_FILE="${TEST_DIR}/sshd_test_host_key"
TEST_CLIENT_KEY_FILE="${TEST_DIR}/ssh_client_key"
TEST_KEY_NAME="SSH_LAUNCHER_TEST_KEY"
TEST_ADDRESS="127.0.0.1"
TEST_PORT="4444"


# -- Create keys
ssh-keygen -t rsa -f "${TEST_HOST_KEY_FILE}" -N ''
ssh-keygen -f "${TEST_CLIENT_KEY_FILE}" -N ''


# -- Compute the key so we can access it
# You need base64 installed
TEST_BASE64_KEY=$(base64 "${TEST_CLIENT_KEY_FILE}")


# -- Launch SSHD

$(which sshd) \
  -f /dev/null \
  -D \
  -p "${TEST_PORT}" \
  -h "${TEST_HOST_KEY_FILE}" \
  -o "ListenAddress=${TEST_ADDRESS}" \
  -o "AuthorizedKeysFile=${TEST_CLIENT_KEY_FILE}.pub" \
  -o StrictModes=no \
  -o LogLevel=DEBUG \
  &
SSHD_PID=$!

echo "SSHD PID is: $SSHD_PID"

function cleanup {
  echo "Performing cleanup"
  rm -r "${TEST_DIR}"
  kill "${SSHD_PID}"
  echo "Cleanup done -- exiting"
}

trap cleanup INT TERM EXIT


# -- Set our SSH Launcher in debug mode (so that it launches the SSH session here)
export SCALR_SSH_LAUNCHER_DEBUG=1

# -- Remove the existing key
rm -f "${HOME}/.ssh/scalr-ssh-keys/${TEST_KEY_NAME}.pem"

# -- Test standard usage
TEST_CMD="echo 'OK' > ${TEST_FILE}"
echo " ${TEST_CMD}" | ${LAUNCH_CMD} -port ${TEST_PORT} -user "${USER}" -host "${TEST_ADDRESS}" -ignoreHostKeys=1 -sshPrivateKey="${TEST_BASE64_KEY}" -sshKeyName="${TEST_KEY_NAME}"


if [ -f "${TEST_FILE}" ]; then
  contents=$(cat "${TEST_FILE}")
  if [ "OK" = "${contents}" ]; then
    echo "Test -- OK!"
    exit 0
  fi
fi

echo "Test -- Failed!"
exit 1
