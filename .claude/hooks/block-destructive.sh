#!/usr/bin/env bash
# Blocks dangerous shell commands before Claude runs them
cmd=$(jq -r '.tool_input.command // empty')

if echo "$cmd" | grep -Eq 'rm -rf|git push --force|git reset --hard|DROP TABLE'; then
  echo "Blocked: destructive command. Ask the user to run it manually." >&2
  exit 2
fi
exit 0