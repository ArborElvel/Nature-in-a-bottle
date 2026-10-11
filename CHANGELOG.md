# Changelog

## 1.0.0

Initial CurseForge and Modrinth release.

- Added the Nature in a Bottle item, converted from Time in a Bottle with bone meal
  (and back with redstone dust); the stored time carries over both ways.
- Right-click a crop to stack the multiplier; the acceleration rate follows Time in
  a Bottle exactly (x1, x2, x4 ... up to x128) while the harvest drops
  `acceleration rate x2` times the produce.
- Three harvest families: age-based crops (`CropBlock` subclasses plus the
  `niab:harvestable_crops` tag), fruiting melon/pumpkin stems, and stacked plants
  (sugar cane, bamboo, cactus) via the `niab:vertical_crops` tag.
- Stop clicking for 3 seconds and the crop is harvested in place; the produce pops
  out, age-based crops return to their youngest age, stems keep growing and stacked
  plants are trimmed down to their bottom block.
- The Time Accelerator Entity is spawned and kept alive for the harvest window, so
  the crop really grows and the multiplier/countdown stays visible.
- Only one bottle in the inventory gathers stored time at a time; while a Time in a
  Bottle is carried, it keeps that job.
- English and Simplified Chinese localization, plus a configurable harvest delay,
  reset behaviour, stored time usage and vertical ripe height.
- Licensed the project under LGPL-3.0-or-later.
