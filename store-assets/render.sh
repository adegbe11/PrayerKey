#!/bin/bash
# Renders frame.html slides to PNG with headless Edge.
# usage: bash render.sh   (writes ios/ and play/)
cd "$(dirname "$0")"
EDGE="/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"
URL="file:///$(pwd -W)/frame.html"
PROFILE="$(cygpath -w "${TMP:-/tmp}")\pk-render-profile"
shot() { # size out hash
  for try in 1 2 3; do
    rm -f "$2"
    "$EDGE" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 --virtual-time-budget=6000 \
      --user-data-dir="$PROFILE" --window-size="$1" --screenshot="$(cygpath -w "$2")" "$URL#$3" 2>/dev/null
    for w in 1 2 3 4 5 6 7 8 9 10; do [ -s "$2" ] && return 0; sleep 1; done
  done
  echo "failed: $2"
}
for i in 1 2 3 4 5 6; do
  shot 1290,2796 "$(pwd)/ios/prayerkey-ios-$i.png" $i
  shot 1080,1920 "$(pwd)/play/prayerkey-play-$i.png" $i
done
