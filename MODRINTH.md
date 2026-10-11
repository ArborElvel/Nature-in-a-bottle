# Modrinth Release Sheet

## Project

- **Name:** Nature in a Bottle
- **Suggested slug:** `nature-in-a-bottle`
- **Summary:** A Time in a Bottle addon that harvests crops for the produce multiplied by the stacked acceleration rate.
- **Project type:** Mod
- **Author:** Unddefined
- **License:** LGPL-3.0-or-later
- **Source:** https://github.com/ArborElvel/NatureInABottle
- **Issue tracker:** https://github.com/ArborElvel/NatureInABottle/issues
- **Environment:** Client and server

## Tags

- Game Mechanics
- Utility
- Food

## Version

- **Name:** Nature in a Bottle 1.0.0
- **Version number:** `1.0.0`
- **Release channel:** Release
- **Game version:** Minecraft 1.21.1
- **Mod loader:** NeoForge
- **File to upload:** `build/libs/niab-1.0.0.jar`
- **Required dependency:** Time in a Bottle 6.5.4 or newer, NeoForge 21.1.238 or newer

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

## Changelog

The complete changelog is maintained in `CHANGELOG.md`. For version `1.0.0`, use:

```markdown
Initial Modrinth release.

- Added the Nature in a Bottle item, convertible to and from Time in a Bottle with stored time carried over.
- Added multiplier stacking that mirrors Time in a Bottle's acceleration rate, and produce multiplied by that rate.
- Added three harvest families: age-based crops, fruiting melon/pumpkin stems, and stacked plants (sugar cane, bamboo, cactus).
- Added the Time Accelerator Entity harvest window, so the rate and countdown stay visible.
- Added English and Simplified Chinese localization.
```

## Upload Checklist

- Build with `.\gradlew.bat clean build`.
- Upload `build/libs/niab-1.0.0.jar`.
- Select `NeoForge` and Minecraft `1.21.1`.
- Set the release channel to `Release`.
- Set the license to `LGPL-3.0-or-later`.
- Add `Time in a Bottle` as a required dependency.
- Add the GitHub source and issue tracker links.
- Add a project icon (already inside the jar as `niab.png`) and in-game screenshots.
- Confirm the required NeoForge version is displayed as `21.1.238+`.
