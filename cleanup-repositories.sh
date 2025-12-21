#!/usr/bin/env bash

set -e

echo "🔍 Searching for repositories { ... } blocks..."

FILES=$(grep -R "^[[:space:]]*repositories[[:space:]]*{" -n . \
  --include="build.gradle.kts" \
  --exclude-dir=".gradle" \
  --exclude-dir="build" \
  | cut -d: -f1 | sort -u)

if [ -z "$FILES" ]; then
  echo "✅ No repositories blocks found. Nothing to clean."
  exit 0
fi

echo "⚠️ Found repositories blocks in:"
echo "$FILES"
echo

read -p "👉 Remove repositories blocks from these files? (y/N): " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo "❌ Aborted."
  exit 1
fi

for file in $FILES; do
  echo "🧹 Cleaning $file"

  # Remove repositories { ... } including nested content
  perl -0777 -i -pe '
    s{
      ^[ \t]*repositories[ \t]*\{
      (?:[^{}]*|\{[^{}]*\})*
      \}[ \t]*\n?
    }{}gmsx
  ' "$file"
done

echo
echo "✅ Cleanup completed."
echo "👉 Run: ./gradlew clean build"
