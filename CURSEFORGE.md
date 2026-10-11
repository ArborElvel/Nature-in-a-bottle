# CurseForge Release Sheet

## Project

- **Name:** Nature in a Bottle
- **Summary:** Turn Time in a Bottle into a harvesting bottle: right-click a crop to stack a multiplier, then collect the produce multiplied by it.
- **Author:** Unddefined
- **License:** LGPL-3.0-or-later
- **Source:** https://github.com/ArborElvel/NatureInABottle
- **Issue tracker:** https://github.com/ArborElvel/NatureInABottle/issues
- **Environment:** Client and server
- **Game version:** Minecraft 1.21.1
- **Mod loader:** NeoForge
- **Required dependency:** Time in a Bottle 6.5.4 or newer, NeoForge 21.1.238 or newer
- **Release type:** Release
- **File to upload:** `build/libs/niab-1.0.0.jar`

## Description

Nature in a Bottle is an addon for Time in a Bottle. Craft the bottle with bone
meal and it becomes a harvesting tool; craft it back with redstone dust at any
time, stored time included.

Right-click a crop with Nature in a Bottle to stack a multiplier on that block.
The acceleration rate escalates exactly like Time in a Bottle does (x1, x2, x4 and
so on, capped at x128) and each click spends stored time with the same formula, so
the crop really grows while you keep clicking.

Stop clicking for three seconds and the crop is harvested on the spot: the produce
is dropped `acceleration rate x2` times over, age-based crops return to their
youngest age, melon and pumpkin stems keep their vine, and sugar cane, bamboo and
cactus are trimmed back to their bottom block.

The mod ships the Time Accelerator Entity for the harvest window, so the rate and
countdown stay visible above the crop, and it never lets two bottles gather stored
time at once.

## Features

- New Nature in a Bottle item, convertible to and from Time in a Bottle (stored
  time carries over).
- Multiplier stacking that mirrors Time in a Bottle's acceleration rate.
- Three harvest families: age-based crops, fruiting melon/pumpkin stems, and
  stacked plants (sugar cane, bamboo, cactus).
- Produce multiplied by the stacked rate, dropped straight onto the ground.
- Configurable harvest delay, crop reset behaviour, stored time usage, passive
  stored time gathering and vertical ripe height.
- English and Simplified Chinese localization.

## Changelog

See `CHANGELOG.md`.

## Upload Checklist

- Build with `.\gradlew.bat clean build`.
- Upload `build/libs/niab-1.0.0.jar`.
- Set the project license to `LGPL-3.0-or-later`.
- Select Minecraft 1.21.1 and NeoForge.
- Mark the file as a Release.
- Add `Time in a Bottle` as a required dependency.
- Add the GitHub repository and issue tracker links.
- Add a project logo (already inside the jar as `niab.png`) and in-game screenshots.
