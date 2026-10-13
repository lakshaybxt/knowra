#!/usr/bin/env bash
input=$(cat)

# Loop guard: if Claude is already continuing because of this hook, let it stop
if [ "$(echo "$input" | jq -r '.stop_hook_active // false')" = "true" ]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR" || exit 0

# Skip if no Java files changed (saves time on chat-only turns)
if [ -z "$(git status --porcelain -- '*.java' 2>/dev/null)" ]; then
  exit 0
fi

# Use the Maven wrapper if the project has one
if [ -x ./mvnw ]; then MVN=./mvnw; else MVN=mvn; fi

output=$($MVN -q test 2>&1)
status=$?

if [ $status -ne 0 ]; then
  echo "Tests failed. Fix them before finishing:" >&2
  echo "$output" | tail -60 >&2
  exit 2
fi
exit 0