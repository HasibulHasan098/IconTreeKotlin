<h1 align="center">IconTree for Kotlin</h1>

<p align="center">
  <b>946 animated icons for Jetpack Compose. Recolorable, searchable, fully offline.</b><br/>
  One line to render. No network, no API keys, no icon fonts.
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Compose" src="https://img.shields.io/badge/Jetpack%20Compose-ready-4285F4?logo=jetpackcompose&logoColor=white">
  <img alt="Icons" src="https://img.shields.io/badge/icons-946-orange">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue">
  <img alt="Offline" src="https://img.shields.io/badge/network-none-success">
</p>

```kotlin
Icon(id = "heart", size = 24.dp, color = Color(0xFFE5484D), mode = IconMode.OnClick)
```

`IconTree for Kotlin` is the Android/Compose sibling of the [`icon-tree-animate`](https://www.npmjs.com/package/icon-tree-animate) npm package. It has the same icon set, the same recolor functions, the same search and the same four playback modes, so web and Android apps can share one icon vocabulary.

## Why IconTree

| | |
|---|---|
| **Animated by default** | 946 Lottie icons, with static, loop, click and hover playback built in. No extra animation code. |
| **100% offline** | Every icon ships inside the AAR's assets. Nothing is downloaded at runtime, so there's no loading spinner, no CDN outage and no privacy question. |
| **Recolor at runtime** | Tint any icon with one `Color`. Recoloring uses Lottie dynamic properties, so the animation is parsed once and a color change costs almost nothing. |
| **Follows your theme** | By default the icon takes `LocalContentColor`, so it matches buttons, tabs and dark mode automatically. |
| **Same API as the web package** | `Icon`, `IconMode`, `recolorLottie`, `getRecoloredLottie`, `IconId` and search are all there. Move between React and Compose without relearning anything. |
| **Searchable** | `IconTree.search("arrow right")` finds icons by name locally, with the same matching rules as `npx icontree search`. |
| **Small surface, light footprint** | One Compose composable plus a single dependency (`lottie-compose`). Hardware-accelerated rendering. Bounded caches. |
| **Tested** | Unit tests check the recolor logic, search behavior, and that the registry matches the icon files on disk and every file is valid Lottie. |

## Install

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.HasibulHasan098.IconTreeKotlin:icon-tree-animate:0.2.6")
}
```

Requires Android `minSdk 24` and Jetpack Compose.

## Quick start

```kotlin
import com.icontree.animate.Icon
import com.icontree.animate.IconMode

@Composable
fun SaveAction(onSave: () -> Unit) {
    Button(onClick = onSave) {
        Icon(id = "heart", size = 24.dp, color = Color(0xFFE5484D), mode = IconMode.OnClick)
        Text("Save")
    }
}
```

### Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `id` | `String` | required | Icon name, such as `"heart"` or `"arrow-right"` |
| `size` | `Dp` | `24.dp` | Width and height |
| `color` | `Color` | `Color.Unspecified` | Tint. `Unspecified` follows `LocalContentColor` (the equivalent of `currentColor`) |
| `mode` | `IconMode` | `Static` | `Static`, `Loop`, `OnClick` or `Hover` |
| `onClick` | `(() -> Unit)?` | `null` | Callback for click playback |
| `trigger` | `IconTrigger?` | `null` | Start playback from another element (the equivalent of `bindId`) |
| `resetAfterPlay` | `Boolean` | `true` | Return to frame zero when a one-shot finishes |
| `modifier` | `Modifier` | `Modifier` | Standard Compose modifier |

### Playback modes

- `Static` shows the first frame.
- `Loop` plays continuously.
- `OnClick` plays once on tap and calls `onClick`.
- `Hover` plays once when a pointer enters the icon (mouse, stylus, ChromeOS, desktop).

### Trigger an icon from another element

```kotlin
val trigger = rememberIconTrigger()

Button(onClick = { trigger.play() }) { Text("Like") }
Icon(id = "heart", mode = IconMode.OnClick, trigger = trigger, onClick = { like() })
```

## Find an icon

```kotlin
IconTree.search("arrow right")   // IDs containing "arrow" or "right"
IconTree.search("heart,star")    // commas and spaces both separate terms
iconIds                          // every bundled ID
```

Search runs against the bundled index. It makes no network requests.

## Recoloring

```kotlin
val tinted: JSONObject = recolorLottie(json, "#2477d4")            // returns a recolored copy
val cached: JSONObject = getRecoloredLottie("heart", json, "#2477d4") // memoized (LRU)
```

These match the npm package: solid fills and strokes are replaced, and the input is never mutated.

## Sample app

The `sample/` module is a gallery that includes search, mode and color switchers, size control and an external trigger demo.

```bash
./gradlew :sample:installDebug
```

## Build and test

```bash
./gradlew :icon-tree-animate:testDebugUnitTest   # unit tests
./gradlew :icon-tree-animate:assembleRelease     # AAR
node scripts/generate-registry.js                # after adding or removing files in icons/
```

## Project layout

```
icons/                 source Lottie JSON (one file per icon ID)
icon-tree-animate/     the library (Compose UI, recolor, search, generated IconIds.kt)
sample/                demo app
scripts/               registry generator
```

## Performance notes

- Animations are parsed once per icon and cached; recoloring is a dynamic property rather than a JSON rewrite.
- Rendering uses `RenderMode.HARDWARE`.
- Looping many icons at once is still real drawing work. Prefer `Static`, `OnClick` or `Hover` in large grids, and test performance in a release build.

## License

The library code is released under the [MIT License](LICENSE).
