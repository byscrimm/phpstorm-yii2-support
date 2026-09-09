#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
version=1.7.12
case "$(uname -s)/$(uname -m)" in
  Darwin/arm64) platform=darwin_arm64; digest=aba9ced2dee8d27fecca3dc7feb1a7f9a52caefa1eb46f3271ea66b6e0e6953f ;;
  Darwin/x86_64) platform=darwin_amd64; digest=5b44c3bc2255115c9b69e30efc0fecdf498fdb63c5d58e17084fd5f16324c644 ;;
  Linux/x86_64) platform=linux_amd64; digest=8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8 ;;
  Linux/aarch64) platform=linux_arm64; digest=325e971b6ba9bfa504672e29be93c24981eeb1c07576d730e9f7c8805afff0c6 ;;
  *) echo "Unsupported actionlint platform" >&2; exit 1 ;;
esac
directory="build/tools/actionlint-$version-$platform"
mkdir -p "$directory"
archive="$directory/archive.tar.gz"
if [[ ! -f "$archive" ]]; then
  curl --fail --silent --show-error --location --retry 3 \
    "https://github.com/rhysd/actionlint/releases/download/v$version/actionlint_${version}_${platform}.tar.gz" \
    --output "$archive.partial"
  mv "$archive.partial" "$archive"
fi
python3 - "$archive" "$digest" <<'PY'
import hashlib, pathlib, sys
if hashlib.sha256(pathlib.Path(sys.argv[1]).read_bytes()).hexdigest() != sys.argv[2]:
    raise SystemExit('actionlint archive checksum mismatch')
PY
tar -xzf "$archive" -C "$directory" actionlint
"$directory/actionlint" -color
