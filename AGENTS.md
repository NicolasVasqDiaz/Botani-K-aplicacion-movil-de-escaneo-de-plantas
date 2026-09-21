# AGENTS.md - Botani-k Project Guidelines

## Project Overview
Botani-k is an educational mobile app for identifying and learning about the endemic flora of Cajamarca, Peru.

## Tech Stack (DO NOT CHANGE WITHOUT APPROVAL)
- Kotlin + Jetpack Compose + Material 3
- Architecture: MVVM + Clean Architecture
- Navigation, DI (Hilt), CameraX
- Networking: Retrofit + OkHttp + Coil
- Firebase: Auth, Firestore, Storage
- Local Cache: Room

## Development Rules (CRITICAL for all agents)
- **Language**: Code in English, UI strings in Spanish.
- **Compose**: State hoisting is mandatory. State lives in ViewModel, never in Composables.
- **Architecture**: Strict separation: UI -> ViewModel -> UseCase -> Repository -> DataSource.
- **Error Handling**: Use sealed classes for results.
- **Code Style**: Follow Kotlin idioms. No XML layouts.
- **Dependencies**: Do not add new dependencies without explicit approval.

## File & Folder Structure
- `app/src/main/java/com/botanik/app/`
  - `data/` (remote, local, repository)
  - `domain/` (model, repository, usecase)
  - `ui/` (scan, plantdetail, garden, profile, auth, theme)
  - `di/`

## What NOT to do
- Do not implement social features, gamification, or multi-language support. This is the MVP.
