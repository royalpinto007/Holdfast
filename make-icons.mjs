/**
 * Generate the launcher icon from one SVG, so every size comes from the same
 * source. Flat fills only: the rasteriser here silently drops gradients.
 */
import { execFileSync } from 'node:child_process'
import fs from 'node:fs'

// A seal: three links of a chain, the last one closed. The mark is the product.
const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128">
  <rect width="128" height="128" rx="28" fill="#0C0D10"/>
  <rect x="30" y="26" width="68" height="20" rx="10" fill="none" stroke="#4A515F" stroke-width="7"/>
  <rect x="30" y="54" width="68" height="20" rx="10" fill="none" stroke="#6B7382" stroke-width="7"/>
  <rect x="30" y="82" width="68" height="20" rx="10" fill="none" stroke="#2FBF71" stroke-width="7"/>
  <circle cx="64" cy="92" r="5" fill="#2FBF71"/>
</svg>`

fs.mkdirSync('icons', { recursive: true })
fs.writeFileSync('icons/icon.svg', svg)
for (const size of [48, 72, 96, 144, 192, 512]) {
  execFileSync('convert', ['-background', 'none', '-density', '900', 'icons/icon.svg',
    '-resize', `${size}x${size}`, `icons/icon-${size}.png`])
}
// Android launcher densities
const dens = { mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192 }
for (const [d, px] of Object.entries(dens)) {
  const dir = `app/src/main/res/mipmap-${d}`
  fs.mkdirSync(dir, { recursive: true })
  for (const name of ['ic_launcher.png', 'ic_launcher_round.png']) {
    execFileSync('convert', ['-background', 'none', '-density', '900', 'icons/icon.svg',
      '-resize', `${px}x${px}`, `${dir}/${name}`])
  }
}
console.log('icons written')
