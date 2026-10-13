#!/usr/bin/env bash
# Formats Java files after Claude edits them
file=$(jq -r '.tool_input.file_path // empty')

if echo "$file" | grep -q '\.java$'; then
  cd "$CLAUDE_PROJECT_DIR" || exit 0
  mvn -q spotless:apply >/dev/null 2>&1 || true
fi
exit 0