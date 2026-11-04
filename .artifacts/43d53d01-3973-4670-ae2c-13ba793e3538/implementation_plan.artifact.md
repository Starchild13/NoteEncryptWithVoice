# NoteEncrypt Modularization and Feature Enhancements Plan

This plan outlines the architectural modularization of the NoteEncrypt app and the addition of note title editing, card reordering (up/down), and layout options (List vs. Grid).

## User Review Required

> [!IMPORTANT]
> **Modularization:** We will integrate the existing `:data` and `:encryption` modules into `settings.gradle.kts` and modularize domain/data/encryption logic.
> **Database Schema Change:** Adding a `title` field to the `Note` entity will bump the Room database version to 2 (with fallback to destructive migration or clear migration strategy).

## Open Questions

- None. All requirements are clear and align with modern Android architecture.

## Proposed Changes

### Module Structure & Build
#### [MODIFY] [settings.gradle.kts](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/settings.gradle.kts)
- Include `:data` and `:encryption` subprojects.

#### [MODIFY] [app/build.gradle.kts](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/app/build.gradle.kts)
- Add dependencies on `:data` and `:encryption` modules.

### Encryption Module
#### [MOVE/REFACTOR] [KeyStoreHelper.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/encryption/src/main/java/com/example/encryption/KeyStoreHelper.kt)
- Move encryption logic to `:encryption` module.

### Data Module
#### [MOVE/REFACTOR] [NoteEntity.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/data/src/main/java/com/example/data/NoteEntity.kt), [NoteDao.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/data/src/main/java/com/example/data/NoteDao.kt), [NoteDatabase.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/data/src/main/java/com/example/data/NoteDatabase.kt), [NoteRepository.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/data/src/main/java/com/example/data/NoteRepository.kt)
- Move Room database and repository logic to `:data` module.
- Update `Note` entity to include `title: String` and `sortOrder: Int`.

### App Module (UI & ViewModel)
#### [MODIFY] [NoteViewModel.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/app/src/main/java/com/example/noteencrypt/NoteViewModel.kt)
- Support note title editing, reordering (move up / move down), and layout toggling.

#### [MODIFY] [SecureNotesUI.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/app/src/main/java/com/example/noteencrypt/SecureNotesUI.kt)
- Add UI controls for note title input, move up/down buttons on cards, and layout toggle (List vs. Grid view).

#### [MODIFY] [EditScreen.kt](file:///Users/jessicarandall/Library/Mobile%20Documents/com~apple~CloudDocs/Downloads/NoteEncrypt-master/app/src/main/java/com/example/noteencrypt/EditScreen.kt)
- Add a text field to edit both note title and note content.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify clean multi-module compilation and dependency resolution.

### Manual Verification
- Verify app launches, creates encrypted notes with titles, reorders cards up/down, and switches between list and grid layouts.
