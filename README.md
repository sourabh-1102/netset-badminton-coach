# NetSet – The Badminton Connect

Coaching app for badminton academies: daily attendance, coach-defined skill criteria (drills) scored 1–10,
match & tournament logs, and 7 / 15 / 30-day performance reports with charts, batch comparison and a
shareable PDF (WhatsApp / share sheet). Licensed & powered by NEXONVATE.

One Kotlin codebase for **Android and iOS** using Compose Multiplatform.

## Project layout

| Folder | What it is |
| --- | --- |
| `shared/` | All app code: screens (Compose), database (Room KMP), report analytics, PDF layout |
| `shared/src/androidMain`, `shared/src/iosMain` | Small platform pieces: files, settings, PDF drawing, sharing, photo/file pickers, PDF preview |
| `app/` | Android app shell (`MainActivity`) |
| `iosApp/` | iOS app shell (SwiftUI) described by `project.yml` for XcodeGen |
| `.github/workflows/build.yml` | Cloud build: Android APK + iOS simulator build with screenshots |

## Android

Requires JDK 17+ and the Android SDK (compileSdk 37).

```bash
./gradlew :app:assembleDebug        # APK in app/build/outputs/apk/debug/
./gradlew :app:installDebug         # install on a connected phone
```

## iOS (needs a Mac with Xcode 16+)

```bash
brew install xcodegen
cd iosApp && xcodegen generate
open NetSet.xcodeproj               # pick a simulator or your iPhone and press Run
```

The Xcode build runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` automatically.
Installing on a real iPhone or TestFlight requires an Apple Developer account for signing.

Without a Mac, every push runs the GitHub Actions workflow, which builds the iOS app on a macOS runner,
launches it in the iPhone Simulator (light and dark mode) and uploads screenshots, a sample PDF and the APK
as build artifacts.
