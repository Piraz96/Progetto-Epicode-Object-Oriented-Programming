#!/bin/sh
# QuizMaster Maven wrapper (minimal POSIX variant).
# Delegates to the system 'mvn' command. If Maven is not installed, prints a
# clear install hint and exits 2 (usage error).
set -e

if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven is not installed on this machine." >&2
    echo "On macOS:   brew install maven" >&2
    echo "On Linux:   sudo apt-get install maven" >&2
    echo "Or download: https://maven.apache.org/download.cgi" >&2
    exit 2
fi

exec mvn "$@"
