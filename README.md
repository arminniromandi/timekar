# TimeKar (تایمکار)

A modern Android task management application with AI-powered voice input, built with Jetpack Compose and Clean Architecture. TimeKar helps users organize their tasks through multiple views: timeline, task list, and calendar, with support for both English and Persian languages.

## Overview

TimeKar is a productivity app that combines traditional task management with AI-assisted task creation. Users can create tasks manually or use voice input to generate tasks through natural language processing. The app features a clean, material design interface with comprehensive scheduling capabilities including reminders, subtasks, priorities, and recurring events.

## Features

### Task Management
- Create, edit, and delete tasks with detailed information
- Support for all-day tasks and time-specific tasks
- Task priorities (Low, Normal, High)
- Categories for organizing tasks (Work, Personal, Finance, Health, etc.)
- Subtask support with completion tracking
- Task completion status with timestamps
- Recurring task scheduling

### Multiple Views
- **Timeline Screen**: Chronological view of scheduled tasks with hourly timeline
- **Tasks Screen**: Organized task lists with filters (All, Today, Upcoming, Overdue, Completed)
- **Calendar Screen**: Monthly calendar view with task indicators
- **Settings Screen**: User preferences and app configuration

### Voice Input & AI Integration
- Voice-to-text task creation using Android Speech Recognition
- AI-powered task parsing through chat completion API
- Real-time voice recognition state feedback
- Automatic task field extraction from natural language

### Reminders & Notifications
- Exact alarm scheduling for task reminders
- Configurable reminder times before task start
- Boot-completed receiver to reschedule alarms after device restart
- Persistent notification support

### Localization
- Full bilingual support (English/Persian)
- RTL layout support for Persian
- Dynamic string resources based on language preference

## Tech Stack

### Core
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material3
- **Build System**: Gradle (AGP 9.0.0) with Kotlin DSL
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Architecture & Libraries
- **Architecture Pattern**: Clean Architecture with MVVM
- **Dependency Injection**: Manual DI with AppContainer pattern
- **Database**: Room 2.7.0 for local persistence
- **Networking**: Retrofit 2.12.0 + OkHttp 4.10.0
- **JSON Parsing**: Moshi 1.15.2 with Kotlin code generation
- **Coroutines**: Kotlinx Coroutines 1.10.2
- **Navigation**: Jetpack Navigation Compose 2.8.9
- **Lifecycle**: ViewModel + LiveData integration

### Code Generation
- KSP (Kotlin Symbol Processing) 2.3.5 for Room and Moshi annotation processing

## Architecture

The project follows Clean Architecture principles with clear separation of concerns:

### Layer Structure

```
app/src/main/java/ir/arminniromandi/timekar/
├── data/              # Data layer
│   ├── local/         # Room database, DAOs, entities
│   ├── remote/        # Retrofit API service, network models
│   ├── repository/    # Repository implementations
│   └── voice/         # Voice recognition manager
├── domain/            # Domain layer
│   ├── model/         # Domain models (TaskItem, Priority, Category)
│   ├── repository/    # Repository interfaces
│   ├── usecase/       # Use cases for business logic
│   └── alarm/         # Reminder scheduling contracts
├── ui/                # Presentation layer
│   ├── screens/       # Screen composables (Timeline, Tasks, Calendar, Settings)
│   ├── components/    # Reusable UI components
│   ├── theme/         # Material3 theming
│   ├── voice/         # Voice UI logic
│   └── shared/        # Shared ViewModels
├── system/            # Android system integration
│   ├── alarm/         # AlarmManager integration
│   ├── notification/  # Notification handling
│   └── receiver/      # Broadcast receivers
├── di/                # Dependency injection container
└── util/              # Utility classes (DateHelper)
```

### Key Design Patterns
- **Repository Pattern**: Abstraction over data sources
- **Use Case Pattern**: Single responsibility business logic units
- **Observer Pattern**: StateFlow/Flow for reactive data streams
- **Dependency Inversion**: Interfaces for all major components
- **Single Activity Architecture**: Compose navigation without fragments

## Project Structure

```
chronos/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ir/arminniromandi/timekar/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml      # Version catalog
│   └── wrapper/
├── build.gradle.kts            # Root build file
├── settings.gradle.kts
├── gradle.properties
└── local.properties            # API keys (not in VCS)
```

## Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2.1 or newer
- JDK 11 or higher
- Android SDK 36
- Gradle 8.x (bundled with wrapper)

### Clone the Repository

```bash
git clone https://github.com/yourusername/chronos.git
cd chronos
```

### Configuration

1. Create a `local.properties` file in the root directory:

```properties
sdk.dir=/path/to/your/Android/Sdk
API_KEY=your_api_key_here
```

2. The `API_KEY` is used for the AI task creation feature. You'll need an API key from an OpenAI-compatible chat completion endpoint.

### Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Select a device or emulator (API 24+)
4. Run the app

Alternatively, build from the command line:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Permissions

The app requires the following permissions:

- `SCHEDULE_EXACT_ALARM` - For precise task reminders
- `RECEIVE_BOOT_COMPLETED` - To reschedule alarms after device restart
- `POST_NOTIFICATIONS` - For task reminder notifications (Android 13+)
- `RECORD_AUDIO` - For voice input feature

All permissions are requested at runtime when needed.

## Implementation Highlights

### Manual Dependency Injection
The app uses a custom `AppContainer` interface with `DefaultAppContainer` implementation instead of Dagger/Hilt. This provides explicit dependency graphs while keeping the codebase simple and compile-time fast.

### Room Database with Seeding
The database is pre-populated with sample tasks on first launch, demonstrating various task types and scheduling patterns. See `AppDatabase.getInitialSeedTasks()` for implementation.

### Voice Recognition Flow
`VoiceToTextManager` wraps Android's `SpeechRecognizer` with coroutine-based state management, providing pause/resume functionality and automatic silence detection for better UX.

### Alarm Persistence
`AndroidTaskReminderScheduler` integrates with AlarmManager to schedule exact alarms. `BootCompletedReceiver` ensures alarms survive device reboots by rescheduling all active reminders.

### Bilingual String Management
`AppStrings` class provides compile-time safe, dynamic string resources based on user language preference, avoiding XML resource inflation overhead.

## Testing

Run unit tests:

```bash
./gradlew test
```

Run instrumented tests:

```bash
./gradlew connectedAndroidTest
```

The project includes:
- Unit tests for use cases and repositories
- Compose UI tests for screens and components
- Coroutines test support with `kotlinx-coroutines-test`

## Build Variants

### Debug
- No code minification
- Debug logging enabled
- Unoptimized for faster builds

### Release
- ProGuard/R8 minification enabled
- Resource shrinking enabled
- Optimized for smaller APK size

## Contributing

Contributions are welcome. Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow Kotlin coding conventions
4. Write tests for new functionality
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Known Limitations

- AI task creation requires external API key configuration
- Voice recognition depends on device-installed speech recognition services
- Exact alarms may be restricted on some devices (Android 12+)

---

**Built with Kotlin & Jetpack Compose**
