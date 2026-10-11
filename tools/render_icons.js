#!/usr/bin/env node
// Regenerates every launcher icon from art/logo.svg. See tools/render_icons.sh for usage.
//
// Two families are produced:
//   * ic_launcher.png / ic_launcher_round.png  -- legacy icons for pre-API-26 launchers.
//   * ic_launcher_foreground.png               -- the adaptive-icon foreground. The canvas is 108dp
//     but only the middle 72dp is guaranteed visible, so the logo is scaled to 66% and centred;
//     anything outside that would be clipped by an aggressive mask (circle, squircle, teardrop).

const fs = require('fs');
const path = require('path');
const { Resvg } = require('@resvg/resvg-js');

const ROOT = path.resolve(__dirname, '..');
const SRC = path.join(ROOT, 'art', 'logo.svg');
const RES = path.join(ROOT, 'app', 'src', 'main', 'res');

// Legacy icon size in px per density bucket.
const LEGACY = {
  'mipmap-mdpi': 48,
  'mipmap-hdpi': 72,
  'mipmap-xhdpi': 96,
  'mipmap-xxhdpi': 144,
  'mipmap-xxxhdpi': 192,
};
// Adaptive foreground canvas: 108dp, so 108 px at mdpi.
const FOREGROUND = {
  'mipmap-mdpi': 108,
  'mipmap-hdpi': 162,
  'mipmap-xhdpi': 216,
  'mipmap-xxhdpi': 324,
  'mipmap-xxxhdpi': 432,
};
// Fraction of the canvas the logo occupies, keeping it inside the 72/108 safe zone.
const SAFE_SCALE = 0.66;

const raw = fs.readFileSync(SRC, 'utf8');
const rootTag = raw.match(/<svg\b[^>]*>/)[0];
// Re-declare the source's extra namespace prefixes; the default SVG namespace comes from the
// wrapper. Without this resvg rejects the nested document ("unknown namespace prefix 'inkscape'").
const extraNs = [...rootTag.matchAll(/\sxmlns(?::[A-Za-z0-9_-]+)?="[^"]*"/g)]
  .map((m) => m[0].trim())
  .filter((d) => !d.startsWith('xmlns="'));
const body = raw
  .replace(/<\?xml[^>]*\?>/, '')
  .replace(/<svg\b[^>]*>/, '')
  .replace(/<\/svg>\s*$/, '');

const render = (svg, size) =>
  new Resvg(svg, { fitTo: { mode: 'width', value: size }, background: 'rgba(0,0,0,0)' })
    .render()
    .asPng();

function wrap(canvas, scale) {
  const inner = canvas * scale;
  const offset = (canvas - inner) / 2;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${canvas}" height="${canvas}" `
    + `viewBox="0 0 ${canvas} ${canvas}">`
    + `<svg x="${offset}" y="${offset}" width="${inner}" height="${inner}" viewBox="0 0 512 512" `
    + `preserveAspectRatio="xMidYMid meet" ${extraNs.join(' ')}>${body}</svg></svg>`;
}

let written = 0;
for (const [dir, size] of Object.entries(LEGACY)) {
  const out = path.join(RES, dir);
  fs.mkdirSync(out, { recursive: true });
  const png = render(raw, size);
  fs.writeFileSync(path.join(out, 'ic_launcher.png'), png);
  fs.writeFileSync(path.join(out, 'ic_launcher_round.png'), png);
  written += 2;
}
for (const [dir, size] of Object.entries(FOREGROUND)) {
  const out = path.join(RES, dir);
  fs.mkdirSync(out, { recursive: true });
  fs.writeFileSync(path.join(out, 'ic_launcher_foreground.png'), render(wrap(size, SAFE_SCALE), size));
  written += 1;
}
console.log(`wrote ${written} icons from ${path.relative(ROOT, SRC)}`);
