# EcoTrace AI 🌿

EcoTrace AI is a modern, feature-rich Android application designed to help users track, analyze, and actively reduce their daily carbon footprint. Built using Jetpack Compose, Material 3, and Room (SQLite), the application features a custom, offline-resilient AI Sustainability Coach powered by Google's **Gemini 3.5 API** via direct REST integrations. 

Users can model hypothetical scenario reductions, track everyday consumption changes, set green goals, unlock gamified badges, and receive real-time personalized tips—creating a streamlined and rewarding sustainability routine.

---

## 🚀 Key Features

*   **Gamified Carbon Dashboard**: View a breakdown of daily estimates, track active goals, monitor eco streaks, and check progress towards custom carbon metrics.
*   **Daily Action Tracker & Multi-log**: Add or remove daily habits (e.g., public transit, walking, vegetarian dishes) with automatic baseline delta updates.
*   **AI Sustainability Coach**: Interactive, context-aware chatbot assisting with custom questions, lifestyle tips, and encouragement (powered by `gemini-3.5-flash`).
*   **Scenario Simulator**: Simulate hypothetical action adjustments changes (e.g., "what if I biked instead of driving 15km every Monday?") and calculate mathematical improvements.
*   **Insights & Trends Analysis**: Explore intuitive, Material 3 progress visualizations detailing home energy, dietary choices, and commute improvements.
*   **Learning Center**: Read curated, bite-sized articles outlining carbon literacy, waste management, and renewable alternatives.
*   **Gamified Unlock System**: Acquire levels and badges (e.g., *Habit Crusader*, *Energy Miser*) as streak achievements get completed.
*   **100% Offline Resilience**: If no internet access or API credentials exist, a built-in local inference and calculation engine automatically supports calculations, coaching questions, and dashboard updates!

---

## 🛠️ Technology Stack

*   **Runtime Language**: 100% Kotlin
*   **UI Framework**: Jetpack Compose (Declarative Android Toolkit)
*   **Design Paradigm**: Material Design 3 (M3) with Edge-to-Edge window handling
*   **Local Persistence**: Room Database (SQLite) with custom database migrations
*   **Asynchronous Flow**: Kotlin Coroutines and Flows (StateFlow/SharedFlow)
*   **Dependency Injection**: Constructor-based repository pattern
*   **Network Client**: Retrofit & OkHttp (for secure, non-blocking REST endpoints)
*   **Config & Secret Management**: Secrets Gradle Plugin (reading from `.env` property sheets)

---

## 📁 Project Folder Structure

```text
EcoTrace AI/
├── app/                          # Main Android Module
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt       # Application Entrypoint & Navigation Flow
│   │   ├── ai/
│   │   │   └── GeminiClient.kt   # Retrofit client and comprehensive offline fallback logic 
│   │   ├── data/
│   │   │   ├── AppDatabase.kt    # SQLite Room Database definition
│   │   │   ├── AppDaos.kt        # Room Data Access Objects for Profile, Tracks, Goals, Badges, Chat
│   │   │   ├── CarbonCalculator.kt # Footprint math and category decomposition utilities
│   │   │   ├── EcoRepository.kt  # Centralized repository combining databases & initial data seed
│   │   │   └── Entities.kt       # Room Entity Definitions
│   │   └── ui/
│   │       ├── EcoViewModel.kt   # Shared application state, flow logic, and background actions
│   │       ├── screens/          # Individual screen composables (Home, Tracker, Insights, Coach, etc.)
│   │       └── theme/            # Material 3 Color Schemes, Typography definitions, and Theme Setup
│   └── build.gradle.kts          # App-level build specifications & package dependencies
├── gradle/                       # Gradle Wrapper configuration and Version Catalogs
│   └── libs.versions.toml        # Unified Dependency/Plugin Version Catalog
├── build.gradle.kts              # Project-level dependencies
├── settings.gradle.kts           # Multi-module root build definitions
├── .env.example                  # Template configuration for collaborative clones
├── .gitignore                    # Industry-standard repository clean exclusions
└── README.md                     # Project documentation (this file)
```

---

## 🔑 Environment Variable Setup

This project uses the secure **Secrets Gradle Plugin** to load configuration variables into the generated `BuildConfig` class at compile time without exposing actual API keys inside source files.

1. Locate the `.env.example` in the root directory. It contains:
   ```properties
   GEMINI_API_KEY=YOUR_API_KEY_HERE
   ```
2. Create a duplicate file named exactly `.env` in the same directory (which is automatically ignored by `.gitignore` to protect your secrets):
   ```bash
   cp .env.example .env
   ```
3. Open your newly created `.env` file and replace `YOUR_API_KEY_HERE` with your active Google Gemini API Key:
   ```properties
   GEMINI_API_KEY=AIzaSyA...YOUR_ACTUAL_KEY...
   ```

---

## 💡 How to Obtain a Gemini API Key

1. Navigate to the Google AI Studio: **[https://aistudio.google.com/](https://aistudio.google.com/)**
2. Sign in with your Google Account.
3. Click on the **"Get API key"** button on the sidebar.
4. Click **"Create API Key"** and choose to link a Google Cloud Project (or create a standard prompt-only key).
5. Copy the generated key and paste it directly into your local `.env` file as described above.

---

## 💻 Running the App Locally

To build, install, and iterate on this project yourself:

### Prerequisites
*   **Android Studio Ladybug (or higher)**
*   **JDK 17 or higher**
*   **Android SDK (Target API 36, Min API 24)**

### Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/ecotrace-ai.git
   cd ecotrace-ai
   ```
2. **Setup the `.env` configuration file**:
   (See the **Environment Variable Setup** section above).
3. **Open in Android Studio**:
   * Launch Android Studio and select `Open an existing project`.
   * Navigate to the `ecotrace-ai` folder and select it.
   * Allow Gradle to run a clean background synchronization.
4. **Compile and Launch**:
   * Attach a physical Android Device with USB Debugging enabled, or boot a Virtual Device Emulator (API level 24+ recommended).
   * Click the **Run** button (green play icon on top bar), or use the hotkey `Shift + F10`.

---

## 📦 Building a Release APK

To distribute a standalone APK or bundle for deployment or side-loading:

Navigate to the project root and execute the standard Gradle assemble command:

```bash
# On Unix-like systems (Linux / macOS / Git Bash)
./gradlew assembleDebug

# Or utilizing the native gradle toolchain
gradle :app:assembleDebug
```

The resulting debug-signed APK will be generated at the following path:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🐞 Common Troubleshooting Steps

### 1. Build Fails with "Local properties not loaded" or "Invalid Secret"
*   **Resolution**: Ensure that the `.env` file is present in the absolute root folder, and that it contains the exact key string: `GEMINI_API_KEY=...`. Note that standard `.properties` configurations or spaces surrounding the `=` character can cause issues. Keep formatting exact.

### 2. Gradle Out Of Memory/Timeout during KSP compilation
*   **Resolution**: Open your local `gradle.properties` file and increase the daemon heap size:
    ```properties
    org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
    ```

### 3. AI Coach chat responds with "Evaluating..." or default fallback advice
*   **Resolution**: This indicates either the app could not find a valid Gemini API key (defaults to fallback) or internet access is missing. Check logcat for tags `GeminiClient` to verify if a valid API key is correctly loaded into `BuildConfig.GEMINI_API_KEY`.

---

## 📄 License

This repository is distributed under the open-source **MIT License**. Check out [LICENSE](LICENSE) for absolute redistribution and private usage conditions.
