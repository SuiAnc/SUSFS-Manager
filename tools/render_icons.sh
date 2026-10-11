#!/usr/bin/env bash
# Regenerates every launcher icon from art/logo.svg.
#
# The rendered PNGs are committed, so this is only needed when the logo changes. It uses
# @resvg/resvg-js because the machine this port was prepared on has no Inkscape, librsvg or
# ImageMagick; a pure-Python rasteriser was tried first and was far too slow for a 107-path SVG.
#
#   npm install @resvg/resvg-js
#   tools/render_icons.sh
set -euo pipefail
cd "$(dirname "$0")/.."
exec node "$(dirname "$0")/render_icons.js"
