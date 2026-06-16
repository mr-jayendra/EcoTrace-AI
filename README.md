# EcoTrace AI 🌿 - Premium Sustainability & Carbon Tracking Platform

Welcome to **EcoTrace AI**, a highly specialized climate-tech ecosystem designed to guide individuals toward zero-carbon lifestyles. Consisting of a native Android core client and a premium glassmorphic web dynamic interface, the application bridges the gap between passive environmental anxiety and proactive ecological habit formation through real-time footprint models and smart generative design.

---

## 🎯 1. Our Chosen Vertical
This project is engineered for the **Environmental Sustainability & Personal Climate Action** vertical. 

### Why Sustainability?
Global carbon reduction targets necessitate active individual participation, yet modern lifestyles hide the immediate ecological cost of daily routines. EcoTrace AI changes this dynamic by transforming opaque scientific coefficients into a colorful, highly interactive, and gamified personal experience. Across transport, diet, energy grid choices, and standby waste, users see exactly how each small adjustment folds into cumulative regional and global savings.

---

## 🧠 2. Approach and Logic

Our architecture is structured around five foundational pillars:

### A. Carbon Footprint Math & Category Decomposition
Rather than displaying a simplistic, arbitrary single-digit score, EcoTrace AI breaks down carbon output into three high-leverage sectors:
*   **Transport Logistics**: Calculations adjust dynamically for passenger-kilometers traveled across diverse transit vectors structure types (e.g., SUVs, compact EVs, heavy buses, subways, and bicycles).
*   **Dietary Choices**: Calculated using livestock methane impact differentials, distinguishing between red meats, poultry, vegetarian, and completely vegan plant profiles.
*   **Grid Electricity Habits**: Analyzes average passive grid consumption combined with active "vampire" standby draws and energy-efficiency behaviors.

### B. "Liquid Glass" Immersive Design Paradigm
To remove the sterile, analytical feel typical of traditional spreadsheets, the web companion features a custom **Liquid Glass Theme**:
*   **Fluid Organic Blends**: Smoothly drifting color blobs animate in the background, signaling natural, fluid ecosystem cycles.
*   **Saturated Glassmorphism**: Cards and panels utilize advanced CSS backdrop filters, light borders, satin sheen highlights, and rich transparency ratios to look modern, clean, and readable.
*   **Contrast Density**: All text structures enforce rigorous contrast pairs, keeping tracking and layout clear for users on standard or dark system states.

### C. Client-Side Resilience & Live LLM Hybrid Architecture
AI agents should never freeze because of poor connectivity. Thus, the intelligence model utilizes a hybrid strategy:
*   **Direct Gemini Integration**: If internet access is available and a client-side Google Gemini API key is configured inside the dynamic settings panel, the coach queries live models (`gemini-1.5-flash`) via secure, non-blocking asynchronous REST layers.
*   **Local Eco-Simulator Fallback**: If offline or keyless, an intelligent local regex and profiles analyzer takes over. It intercepts query vectors and renders highly precise, mathematically sound lifestyle tips mapped perfectly to the user’s real habit configurations.

### D. Gamified Progress Circles & Streak Incentives
Habit formation leverages psychological positive reinforcement. The app features a progressive badge engine (*Habit Crusader*, *Energy Miser*, *Zero Emission Sage*) connected directly to logging streaks and habit updates, converting sustainable action into an engaging game.

---

## 🛠️ 3. How the Solution Works

### A. Native Android Client (`com.aistudio.ecotrace.shqep`)
*   **Tech Stack**: 100% Kotlin, Jetpack Compose, Material Design 3, Room SQLite, Kotlin Coroutines, Kotlin Serialization, and Retrofit.
*   **State Pipeline**: Uses `MutableStateFlow` inside a shared `EcoViewModel` adhering to unidirectional data flow (UDF).
*   **Local Persistence**: High-performance local SQL database managed via Room. All tracking states, badges unlocked, custom goals, and chat transcripts persist gracefully across application cycles.
*   **Edge-to-Edge**: Custom topbar, scaffold padding limits, and high-DPI resolution icons matching premium Material design rules.

### B. Web Companion Interface (`/docs`)
The web companion is a lightweight, responsive single-page web app built to showcase EcoTrace's premium design elements.
*   **Platform Sync**: The client-side database loads and saves dynamic settings directly in browser LocalStorage.
*   **Liquid Glass Theme (`style.css`)**: Overrides background patterns using radial gradients and slow keyframe drift animations (`driftBlobs`). Utilizes high-saturation backdrop filters to render clean glass dashboards.
*   **Floating AI Coach Chatbot**:
    *   Initiated or revealed using an animated circular Floating Action Button (FAB) styled with a shining glass reflection and ping alerts.
    *   The chat UI is contained entirely in a beautiful floating card panel that scale-transforms from the bottom corner of the viewport.
    *   Features quick-selection prompt chips for rapid analysis queries without keyboard input.
    *   Provides synchronous interaction updates: user chats instantly add bubbles, the chatbot presents a glowing "analyzing carbon profile" typing status, and replies with formatted, markdown-parsed sustainability coaching logs.

---

## 📋 4. Key Assumptions Made

To ensure mathematical consistency and smooth system execution across both Android and Web build platforms, we established the following design assumptions:

1.  **Ecological Emission Baselines**:
    *   Standard daily baselines utilize recognized ecological guidelines (e.g., driving a medium petroleum car emits roughly `0.18 kg CO₂e` per kilometer; a standard heavy meat-diet generates roughly `6.5 kg CO₂e` daily, whereas a vegan diet generates `1.5 kg CO₂e`).
    *   While individual localized climates vary, using these standardized coefficients allows predictable mathematical outputs for users modeling scenario changes.
2.  **Stateless API Security**:
    *   Client-side Google Workspace / Google Cloud Gemini keys are not hardcoded or sent to intermediary servers. They run directly over clean browser-to-server REST headers or stored variables within secure `.env` files to maintain user confidentiality.
3.  **Local Storage as Root of Web Truth**:
    *   To mirror a true client database without server databases on static web builds, LocalStorage is assumed to be consistently available, enabling data persistence across browser sessions.
4.  **Device Compatibility**:
    *   The Web app assumes modern browser engines supporting standard CSS variables, keyframe animations, and `backdrop-filter` styles (standard on Chrome, Safari, and Firefox).

---

## 📁 Project Folder Structure

```text
EcoTrace AI/
├── app/                          # Main native Android application module
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt       # Android Screen router and navigation flow
│   │   ├── ai/
│   │   │   └── GeminiClient.kt   # Retrofit gateway and fallback simulator
│   │   ├── data/
│   │   │   ├── AppDatabase.kt    # SQLite Room definitions
│   │   │   ├── CarbonCalculator.kt # Emission coefficients and math models
│   │   │   └── Entities.kt       # Profile and task log entities
...
├── docs/                         # Dynamic Web Dashboard companion
│   ├── index.html                # Main layout, responsive panels, & Floating AI Coach UI
│   ├── style.css                 # Custom glassmorphism, drifting blobs, and glowing FAB styles
│   └── script.js                 # Dynamic JS state engine, chatbot handles, & LocalStorage sync
├── build.gradle.kts              # Project-level dependencies
├── settings.gradle.kts           # Multi-module root paths
└── README.md                     # Central documentation file (this document)
```

---

## 🚀 Getting Started

### 🖥️ 1. Exploring the Web Experience
To interact with the premium liquid glass layout and floating AI Sustainability Coach:
1. Double-click **`/docs/index.html`** or navigate to the shared web preview URL.
2. Click the floating green chat bubble (`💬`) in the bottom-right corner to open the AI Coach!
3. Tap on quick-prompt chips like *"Recommend 3 Eco-friendly habits"* or enter custom text.
4. Open the **Settings Tab** on the main glass panel to enter an optional private Gemini API key to activate live generative responses from `gemini-1.5-flash`.

### 📱 2. Compiling the Android Core App
1. Load the project in Android Studio.
2. Configure your Gemini API key in your secure local `.env` property sheets at the workspace root folder.
3. Link an active physical Android device via USB debugging or spin up a Virtual Emulator.
4. Execute `./gradlew assembleDebug` or use the IDE play trigger to deploy instant native APKs!

---

*Formulated with passion for clean ecosystems and high-fidelity interface design.* 🌿
