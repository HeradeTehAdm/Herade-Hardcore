# Herade Hardcore

Client-only Fabric mod for Minecraft 26.3, intended for singleplayer.

## Preparation sequence

- The player is restored to 10 hearts when a world is entered.
- A 20-second preparation timer starts.
- During preparation, block breaking, block placement, item use, attacks, and item pickup are blocked.
- When the timer ends, the title **ХАРДКОР НАЧАЛСЯ!** appears.
- The chat receives: **Задача: пройти Minecraft или смерть!**
- Preparation restrictions are then lifted.

## Build

Requires JDK 25 and Gradle 9.6.0.

```bash
gradle build
```

The mod is marked client-only in `fabric.mod.json`. It does not include a dedicated-server entrypoint.

## Current status

Initial implementation committed. The GitHub Actions build must complete successfully, and the mechanics still need in-game verification on Minecraft 26.3.
