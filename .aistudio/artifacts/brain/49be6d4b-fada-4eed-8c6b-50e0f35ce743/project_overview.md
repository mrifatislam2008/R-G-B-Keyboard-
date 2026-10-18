# Rimu Keyboard — Native Android IME Project

## 1. Project Summary
**Rimu Keyboard** is a full-featured, native Android Input Method Editor (IME) application built in Kotlin and Jetpack Compose. It operates both as an independent customization dashboard application and as a system-registered keyboard service capable of typing into any Android application.

## 2. Architecture & File Structure

```
app/src/main/
├── AndroidManifest.xml                         # System declaration with BIND_INPUT_METHOD
├── res/
│   ├── xml/
│   │   └── method.xml                          # IME metadata, subtypes (EN, BN), settings activity
│   ├── drawable/
│   │   ├── rimu_logo.jpg                       # Custom RGB mechanical keyboard emblem
│   │   ├── ic_launcher_background.xml          # Adaptive icon background (#0D1117)
│   │   └── ic_launcher_foreground.xml          # Adaptive icon foreground
│   └── values/
│       └── strings.xml                         # UI strings, subtype labels, descriptions
└── java/com/example/
    ├── MainActivity.kt                         # Dashboard, setup wizard & live interactive testing
    ├── ime/
    │   ├── RimuInputMethodService.kt           # Core InputMethodService implementation
    │   └── ImeLifecycleOwner.kt                # Lifecycle & SavedState provider for Compose in IME
    ├── keyboard/
    │   ├── KeyboardModels.kt                   # KeyModel, KeyType, ShiftState, EmojiCategory enums
    │   └── KeyboardLayouts.kt                  # English QWERTY, Bangla layout, Symbols & Emojis
    ├── rgb/
    │   └── RgbAnimationEngine.kt               # 7 RGB animation modes, gradient engine & key glows
    ├── audio/
    │   └── SoundHapticManager.kt               # 5 PCM sound profiles & vibration haptic feedback
    ├── data/
    │   ├── KeyboardPreferences.kt              # Local SharedPreferences store with StateFlow
    │   ├── SuggestionEngine.kt                 # English & Bangla predictive text & emoji triggers
    │   └── ClipboardManagerHelper.kt           # On-device local clipboard manager
    ├── ui/
    │   ├── keyboard/
    │   │   └── RimuKeyboardView.kt             # Jetpack Compose keyboard rendering
    │   ├── screens/
    │   │   ├── HomeScreen.kt                   # Setup instructions, test box & quick RGB chips
    │   │   ├── RgbAppearanceScreen.kt          # RGB modes, sliders, neon palette & key shape
    │   │   ├── SoundHapticsScreen.kt           # Sound profiles, volume & vibration controls
    │   │   └── InputAboutScreen.kt             # Language, suggestions, privacy declaration & about
    │   └── theme/
    │       ├── Color.kt                        # RGB neon gaming palette & keycap surface colors
    │       └── Theme.kt                        # Dark Cyberpunk Material 3 theme
```

## 3. Verified System Integration

- **System IME Service**: `RimuInputMethodService` extends Android's `InputMethodService` and is declared in `AndroidManifest.xml` with `android.permission.BIND_INPUT_METHOD`.
- **IME Metadata**: Defined in `res/xml/method.xml` declaring:
  - English subtype (`en_US`)
  - Bangla subtype (`bn_BD`)
  - Direct link to `MainActivity` as `android:settingsActivity`
  - Inline suggestions enabled for autofill
- **Text & Input Handling**:
  - `commitText` and selection-aware deletion
  - Dynamic Enter key action resolution (Search 🔍, Send ➤, Go ➔, Next ⇥, Done ✓, Enter ↵)
  - Auto-capitalization and sentence detection
  - Password field detection to protect privacy and suppress suggestion caching
  - Next IME switching (`switchToNextInputMethod`)
  - Atomic batch editing (`beginBatchEdit` / `endBatchEdit`)

## 4. Privacy Guarantee
- 100% on-device processing.
- Zero network permissions requested in `AndroidManifest.xml`.
- No ads, analytics, tracking, or remote telemetry.
- Passwords and sensitive input are never recorded.

## 5. Build Artifact
- **Target APK Path**: `app/build/outputs/apk/debug/app-debug.apk`
- **Build Status**: Verified via Gradle `assembleDebug` (23 MB).
