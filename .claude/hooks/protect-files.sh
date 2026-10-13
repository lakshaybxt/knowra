#!/usr/bin/env bash
# Blocks edits to secrets and production config
file=$(jq -r '.tool_input.file_path // empty')

if echo "$file" | grep -Eq '(^|/)\.env|application-prod'; then
  echo "Blocked: $file is a protected file. Ask the user to edit it manually." >&2
  exit 2
fi
exit 0