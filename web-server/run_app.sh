#!/usr/bin/env bash
export DISPLAY=:99
Xvfb -ac :99 -screen 0 1280x1024x16 &
/breezy-web-server-1.0-SNAPSHOT/bin/breezy-web-server