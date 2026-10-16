# ChangeLens — Course Topic Mapping

All 29 Android course topics implemented in ChangeLens.

---

## 01.2 Layouts and resources for the UI

**Feature:** Full XML layout system with responsive grid configuration.
**Files:**
- `app/src/main/res/layout/activity_main.xml` — Dashboard with CoordinatorLayout, search, filter chips, RecyclerView, empty state
- `app/src/main/res/layout/activity_project_detail.xml` — Project header card, timeline, bottom selection bar
- `app/src/main/res/layout/activity_compare.xml` — Comparison view with mode toggle, seek bar, annotations list
- `app/src/main/res/layout/activity_edit_project.xml` — Validated project form
- `app/src/main/res/layout/activity_settings.xml` — Settings screen
- `app/src/main/res/layout/activity_photo_viewer.xml` — Full-screen photo viewer
- `app/src/main/res/layout/activity_help.xml` — Scrollable help with onboarding
- `app/src/main/res/layout/activity_education.xml` — Course demos screen
- `app/src/main/res/layout/item_project.xml` — Project card with thumbnail, category chip, overflow menu
- `app/src/main/res/layout/item_photo.xml` — Timeline photo item with selection checkbox
- `app/src/main/res/layout/item_annotation.xml` — Annotation row with colored dot and delete button
- `app/src/main/res/layout/dialog_photo_details.xml` — Photo caption/notes/date dialog
- `app/src/main/res/layout/dialog_annotation.xml` — Annotation text input dialog
- `app/src/main/res/values-sw600dp/integers.xml` — Tablet 2-column grid override

**Demonstration:** Launch app → observe dashboard layout. Rotate device or use tablet emulator → grid adapts.
**Verification:** `AppLaunchTest.mainActivity_launches()` passes. Manual verification on emulator confirms layout.
**Status:** Production feature.

---

## 01.3 Text and scrolling views

**Feature:** Scrollable project descriptions, timeline, help text, and photo metadata.
**Files:**
- `app/src/main/res/layout/activity_main.xml` — NestedScrollView wrapping empty state
- `app/src/main/res/layout/activity_project_detail.xml` — Scrollable header with description
- `app/src/main/res/layout/activity_help.xml` — Full scrollable help content
- `app/src/main/res/layout/item_project.xml` — Description text with maxLines
- `app/src/main/res/layout/activity_photo_viewer.xml` — Metadata overlay

**Demonstration:** Open project detail → scroll through description. Open help → scroll through all steps.
**Verification:** Manual verification on emulator confirms scrolling works.
**Status:** Production feature.

---

## 01.4 Resources to help you learn

**Feature:** Onboarding dialog, help screen with 4-step guide, offline info, and course demos access.
**Files:**
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — Shows onboarding dialog on first launch
- `app/src/main/java/com/changelens/app/ui/HelpActivity.java` — Static help with steps and offline info
- `app/src/main/res/layout/activity_help.xml` — Help layout
- `app/src/main/res/layout/activity_education.xml` — Demos access point
- `app/src/main/res/values/strings.xml` — `help_onboarding_title`, `help_step1`–`help_step4`, `help_offline_title`

**Demonstration:** First launch → onboarding dialog appears. Menu → Help → full guide with demos button.
**Verification:** Manual verification confirmed onboarding dialog shows and help screen displays.
**Status:** Production feature.

---

## 02.1 Activities and Intents

**Feature:** Seven activities with explicit navigation via Intent.
**Files:**
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — Dashboard launcher
- `app/src/main/java/com/changelens/app/ui/ProjectDetailActivity.java` — Project timeline
- `app/src/main/java/com/changelens/app/ui/EditProjectActivity.java` — Create/edit project
- `app/src/main/java/com/changelens/app/ui/CompareActivity.java` — Before/after comparison
- `app/src/main/java/com/changelens/app/ui/PhotoViewerActivity.java` — Full-screen viewer
- `app/src/main/java/com/changelens/app/ui/SettingsActivity.java` — Settings
- `app/src/main/java/com/changelens/app/ui/HelpActivity.java` — Help
- `app/src/main/AndroidManifest.xml` — All activity declarations with parentActivityName

**Demonstration:** Navigate dashboard → project detail → compare → photo viewer → settings → help.
**Verification:** `AppLaunchTest` confirms activity launches. Manual navigation tested on emulator.
**Status:** Production feature.

---

## 02.2 Activity lifecycle and state

**Feature:** State restoration after rotation and process recreation via ViewModel.
**Files:**
- `app/src/main/java/com/changelens/app/ui/EditProjectActivity.java` — Inner `EditViewModel` survives rotation, preserves form state
- `app/src/main/java/com/changelens/app/ui/ProjectsViewModel.java` — AndroidViewModel with MediatorLiveData
- `app/src/main/java/com/changelens/app/data/AppDatabase.java` — Singleton database with `setInstance()` for test injection

**Demonstration:** Open edit project form → enter data → rotate device → data preserved.
**Verification:** ViewModel pattern confirmed. `AppDatabase.setInstance()` supports test injection.
**Status:** Production feature.

---

## 02.3 Implicit Intents

**Feature:** Image/document selection, camera capture, and native sharing via implicit intents.
**Files:**
- `app/src/main/java/com/changelens/app/ui/ProjectDetailActivity.java` — `ActivityResultLauncher` for gallery import and camera capture
- `app/src/main/java/com/changelens/app/util/ShareUtils.java` — FileProvider-based sharing via `Intent.ACTION_SEND`
- `app/src/main/java/com/changelens/app/util/ImageStore.java` — URI import to managed storage
- `app/src/main/AndroidManifest.xml` — Camera feature declaration (not required)
- `app/src/main/res/xml/file_paths.xml` — FileProvider paths

**Demonstration:** Project detail → Add photo → Import from gallery (implicit intent). Compare → Share image (implicit intent).
**Verification:** Gallery import and share tested. FileProvider configured correctly.
**Status:** Production feature.

---

## 03.1 Android Studio debugger

**Feature:** Privacy-safe logging and reproducible debug scenarios.
**Files:**
- `app/src/main/java/com/changelens/app/util/LocaleHelper.java` — Locale resolution logging points
- `app/src/main/java/com/changelens/app/util/ImageStore.java` — Image decode error logging
- `app/src/main/java/com/changelens/app/data/ProjectRepository.java` — Operation logging
- `app/src/main/java/com/changelens/app/service/ReportExportService.java` — Export progress logging

**Demonstration:** Connect debugger → set breakpoints in repository methods → inspect LiveData values.
**Verification:** Debug build compiles and runs. Logcat shows privacy-safe operation logs (no PII).
**Status:** Production feature.

---

## 03.2 App testing

**Feature:** Unit tests for core logic and instrumentation tests for UI workflows.
**Files:**
- `app/src/test/java/com/changelens/app/ProjectValidatorTest.java` — 11 tests for form validation
- `app/src/test/java/com/changelens/app/FormattersTest.java` — 7 tests for date/file-size formatting
- `app/src/test/java/com/changelens/app/EntityModelTest.java` — 9 tests for entity field integrity
- `app/src/test/java/com/changelens/app/ComparisonLogicTest.java` — 17 tests for timeline sorting, selection, divider math
- `app/src/androidTest/java/com/changelens/app/AppLaunchTest.java` — 6 instrumentation tests for activity launch and UI elements

**Demonstration:** `./gradlew testDebugUnitTest` → 44 tests pass. `./gradlew connectedDebugAndroidTest` → 6 tests pass.
**Verification:** All tests pass (44 unit + 6 instrumentation = 50 total, 0 failures).
**Status:** Production feature.

---

## 03.3 Android Support Library

**Feature:** AndroidX components throughout: AppCompat, RecyclerView, Lifecycle, Room, Activity, Fragment.
**Files:**
- `app/build.gradle.kts` — All AndroidX dependencies
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — AppCompatActivity
- `app/src/main/java/com/changelens/app/ui/ProjectsAdapter.java` — RecyclerView.Adapter with DiffUtil
- `app/src/main/java/com/changelens/app/data/AppDatabase.java` — Room database
- `app/src/main/java/com/changelens/app/util/LocaleHelper.java` — AppCompatDelegate for per-app locales

**Demonstration:** All screens use AppCompat. RecyclerView with DiffUtil for efficient updates. Room for persistence.
**Verification:** Build succeeds with all AndroidX dependencies. App runs on API 24+ emulator.
**Status:** Production feature.

---

## 04.1 Buttons and clickable images

**Feature:** Capture, selection, comparison, annotation, and export buttons throughout the app.
**Files:**
- `app/src/main/res/layout/activity_main.xml` — FAB for project creation
- `app/src/main/res/layout/activity_project_detail.xml` — FAB for photo import, compare/clear buttons
- `app/src/main/res/layout/activity_compare.xml` — Mode toggle, swap, annotate, export buttons
- `app/src/main/res/layout/activity_edit_project.xml` — Save, date pickers, cover picker buttons
- `app/src/main/res/layout/item_project.xml` — Overflow menu button
- `app/src/main/res/layout/item_photo.xml` — Selection checkbox

**Demonstration:** Dashboard → FAB → create project. Project detail → FAB → add photo. Compare → slider/overlay toggle → annotate → export.
**Verification:** All buttons tested on emulator. Project creation workflow verified.
**Status:** Production feature.

---

## 04.2 Input controls

**Feature:** Validated forms with title, description, category spinner, date pickers, captions, and notes.
**Files:**
- `app/src/main/java/com/changelens/app/ui/EditProjectActivity.java` — Form with validation
- `app/src/main/java/com/changelens/app/util/ProjectValidator.java` — Pure validation logic (title required/max 80, description max 500, target date >= start)
- `app/src/main/res/layout/activity_edit_project.xml` — TextInputLayout with error display
- `app/src/main/res/layout/dialog_photo_details.xml` — Caption/notes inputs with date picker
- `app/src/main/java/com/changelens/app/util/SimpleTextWatcher.java` — Text change listener

**Demonstration:** Create project → leave title empty → save → error shown. Enter long title → error shown.
**Verification:** `ProjectValidatorTest` — 11 tests pass. Manual form validation tested on emulator.
**Status:** Production feature.

---

## 04.4 User navigation

**Feature:** Predictable navigation with toolbar back buttons, parent activity declarations, and bottom selection bar.
**Files:**
- `app/src/main/AndroidManifest.xml` — All activities have `parentActivityName`
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — Toolbar with sort/help/settings menu
- `app/src/main/java/com/changelens/app/ui/ProjectDetailActivity.java` — Toolbar back navigation, bottom compare/clear bar
- `app/src/main/java/com/changelens/app/ui/CompareActivity.java` — Toolbar back, mode toggle
- `app/src/main/java/com/changelens/app/ui/SettingsActivity.java` — Toolbar back

**Demonstration:** Navigate forward through screens → toolbar back button returns to previous screen. System back works identically.
**Verification:** Navigation tested on emulator. All back buttons return to correct parent.
**Status:** Production feature.

---

## 04.5 RecyclerView

**Feature:** Project cards grid and chronological photo list with RecyclerView + DiffUtil.
**Files:**
- `app/src/main/java/com/changelens/app/ui/ProjectsAdapter.java` — ListAdapter with DiffUtil, thumbnail caching, async decode
- `app/src/main/java/com/changelens/app/ui/TimelineAdapter.java` — ListAdapter with DiffUtil, multi-select mode
- `app/src/main/java/com/changelens/app/ui/AnnotationsAdapter.java` — ListAdapter with DiffUtil, delete support
- `app/src/main/java/com/changelens/app/ui/ProjectsViewModel.java` — MediatorLiveData combining search/filter/sort
- `app/src/main/res/layout/item_project.xml` — Project card layout
- `app/src/main/res/layout/item_photo.xml` — Photo item layout
- `app/src/main/res/layout/item_annotation.xml` — Annotation item layout

**Demonstration:** Dashboard → project cards in RecyclerView. Project detail → timeline photos in RecyclerView. Search/filter → list updates reactively.
**Verification:** Adapters use DiffUtil for efficient updates. ViewModel tested with search/filter/sort combinations.
**Status:** Production feature.

---

## 05.1 Drawables, styles, and themes

**Feature:** Material 3 theming with brand colors, vector drawables, and reusable styles.
**Files:**
- `app/src/main/res/values/colors.xml` — 30 color resources (teal primary, brown secondary, full M3 palette)
- `app/src/main/res/values/themes.xml` — Theme.ChangeLens (Material3.DayNight.NoActionBar) with color mapping
- `app/src/main/res/values-night/themes.xml` — Dark theme override
- `app/src/main/res/values-night/colors.xml` — Dark mode colors
- `app/src/main/res/drawable/` — 31 vector drawables (ic_lens, ic_add, ic_camera, ic_compare, etc.)
- `app/src/main/res/drawable/bg_annotation_dot.xml` — Annotation marker background
- `app/src/main/res/drawable/bg_chip.xml` — Category chip background
- `app/src/main/res/values/dimens.xml` — 11 dimension resources

**Demonstration:** Launch app → observe branded teal theme. Toggle dark mode in settings → colors adapt.
**Verification:** Theme switching tested on emulator. All screens render with correct colors.
**Status:** Production feature.

---

## 05.2 Material Design

**Feature:** Material 3 components: MaterialCardView, TextInputLayout, ChipGroup, MaterialButton, FloatingActionButton.
**Files:**
- `app/src/main/res/layout/activity_main.xml` — ChipGroup for filters, FAB for creation
- `app/src/main/res/layout/activity_project_detail.xml` — MaterialCardView for header, MaterialButton for actions
- `app/src/main/res/layout/activity_edit_project.xml` — TextInputLayout with hints and errors
- `app/src/main/res/layout/activity_compare.xml` — MaterialButtonToggleGroup for mode selection
- `app/src/main/res/layout/activity_settings.xml` — Material Switch for reminders

**Demonstration:** All screens use consistent M3 components, typography, spacing, and accessible touch targets (48dp min).
**Verification:** UI inspected on emulator — all Material components render correctly.
**Status:** Production feature.

---

## 05.3 Resources for adaptive layouts

**Feature:** Tablet-optimized grid layout with sw600dp qualifier.
**Files:**
- `app/src/main/res/values/integers.xml` — `projects_span_count = 1` (phone)
- `app/src/main/res/values-sw600dp/integers.xml` — `projects_span_count = 2` (tablet)
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — GridLayoutManager with dynamic span count

**Demonstration:** Phone emulator → single-column project list. Tablet (sw600dp+) → two-column grid.
**Verification:** Span count resource confirmed. Layout adapts to screen width.
**Status:** Production feature.

---

## 06.1 UI testing

**Feature:** Automated Espresso tests for major user workflows.
**Files:**
- `app/src/androidTest/java/com/changelens/app/AppLaunchTest.java` — 6 tests: activity launch, app name display, empty state, FAB visibility, search visibility, filter chips visibility
- `app/build.gradle.kts` — Espresso dependencies (espresso-core, ext-junit, test-runner, test-rules)

**Demonstration:** `./gradlew connectedDebugAndroidTest` → 6 tests pass on emulator.
**Verification:** All 6 instrumentation tests pass (0 failed).
**Status:** Production feature.

---

## 07.1 AsyncTask and AsyncTaskLoader

**Feature:** Educational demonstration of legacy APIs, isolated from production code. Production uses executors.
**Files:**
- `app/src/main/java/com/changelens/app/education/LegacyAsyncTaskDemo.java` — Deprecated AsyncTask with progress dialog
- `app/src/main/java/com/changelens/app/education/LegacyLoaderDemo.java` — AsyncTaskLoader via LoaderManager
- `app/src/main/java/com/changelens/app/ui/EducationActivity.java` — Demo runner with cancel-on-destroy
- `app/src/main/java/com/changelens/app/util/AppExecutors.java` — Production thread pool (diskIO, networkIO, main)
- `app/src/main/res/layout/activity_education.xml` — Demo buttons and result display

**Demonstration:** Help → Course demos → Run AsyncTask demo → observe progress dialog → completion. Run Loader demo → tips loaded.
**Verification:** Both demos execute on emulator. AsyncTask cancelled in onDestroy (no leak).
**Status:** Educational demonstration (legacy APIs isolated in `education` package).

---

## 07.2 Internet connection

**Feature:** Offline-first architecture with optional connectivity status banner.
**Files:**
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — ConnectivityManager.NetworkCallback, offline/online banner
- `app/src/main/java/com/changelens/app/ui/EducationActivity.java` — Connectivity status check
- `app/src/main/AndroidManifest.xml` — `ACCESS_NETWORK_STATE` permission
- `app/src/main/res/values/strings.xml` — `offline_banner`, `online_banner`

**Demonstration:** Disable WiFi on emulator → offline banner appears. Re-enable → "Back online" banner shows.
**Verification:** Network callback tested. All core features work without internet.
**Status:** Production feature.

---

## 07.3 Broadcasts

**Feature:** System broadcasts for boot completion and time changes to reschedule reminders.
**Files:**
- `app/src/main/java/com/changelens/app/reminder/BootReminderReceiver.java` — BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED
- `app/src/main/java/com/changelens/app/reminder/ReminderReceiver.java` — Alarm trigger receiver
- `app/src/main/AndroidManifest.xml` — Receiver declarations with intent filters

**Demonstration:** Reboot device → boot receiver reschedules daily reminder. Change time → alarm rescheduled.
**Verification:** Receivers registered in manifest. Broadcast handling confirmed.
**Status:** Production feature.

---

## 07.4 Services

**Feature:** Genuine background service for report export and JobScheduler for deferred maintenance.
**Files:**
- `app/src/main/java/com/changelens/app/service/ReportExportService.java` — Background Service building HTML report on diskIO thread, posts notification with share action
- `app/src/main/java/com/changelens/app/service/ThumbnailCleanupJobService.java` — JobScheduler job (6-hour interval) cleaning orphaned thumbnails
- `app/src/main/java/com/changelens/app/util/AppExecutors.java` — Production thread pool alternative
- `app/src/main/AndroidManifest.xml` — Service declarations

**Demonstration:** Project detail → Export report → service runs in background → notification with share action appears. JobScheduler runs cleanup every 6 hours.
**Verification:** Export service tested on emulator — HTML report generated and notification posted.
**Status:** Production feature.

---

## 08.1 Notifications

**Feature:** Notification channels for progress reminders and export completion.
**Files:**
- `app/src/main/java/com/changelens/app/util/NotificationHelper.java` — Channel creation (`reminders`, `exports`), `notifyCompat()` with permission check
- `app/src/main/java/com/changelens/app/reminder/ReminderReceiver.java` — Posts progress notification on alarm
- `app/src/main/java/com/changelens/app/service/ReportExportService.java` — Posts export-ready notification with share action
- `app/src/main/AndroidManifest.xml` — `POST_NOTIFICATIONS` permission
- `app/src/main/res/values/strings.xml` — Channel names and descriptions

**Demonstration:** Enable reminders in settings → daily notification at set time. Export report → notification appears when ready.
**Verification:** Notification channels created on app start. Export notification tested on emulator.
**Status:** Production feature.

---

## 08.2 Alarms

**Feature:** User-controlled daily reminder alarms with inexact repeating (respects Doze/App Standby).
**Files:**
- `app/src/main/java/com/changelens/app/reminder/ReminderScheduler.java` — `scheduleDaily()`, `cancel()`, `rescheduleIfEnabled()`
- `app/src/main/java/com/changelens/app/reminder/ReminderReceiver.java` — Alarm trigger → notification
- `app/src/main/java/com/changelens/app/reminder/BootReminderReceiver.java` — Reschedule after boot/time change
- `app/src/main/java/com/changelens/app/data/SettingsRepository.java` — Reminder hour/minute persistence

**Demonstration:** Settings → enable reminders → set time → daily alarm fires at scheduled time.
**Verification:** Inexact repeating alarm used (no SCHEDULE_EXACT_ALARM permission needed). Reschedule on boot confirmed.
**Status:** Production feature.

---

## 08.3 Efficient data transfer and JobScheduler

**Feature:** JobScheduler for deferred thumbnail cleanup with persistence across reboots.
**Files:**
- `app/src/main/java/com/changelens/app/service/ThumbnailCleanupJobService.java` — JobService with 6-hour interval, persisted, orphan thumbnail cleanup
- `app/src/main/java/com/changelens/app/util/ImageStore.java` — `cleanupOrphanThumbs()` implementation
- `app/src/main/AndroidManifest.xml` — `BIND_JOB_SERVICE` permission
- `app/src/main/java/com/changelens/app/ui/EducationActivity.java` — Demo trigger

**Demonstration:** App startup → cleanup job scheduled. Job runs every 6 hours → orphaned thumbnails removed.
**Verification:** Job scheduled on app start. Manual trigger via Education → demos.
**Status:** Production feature.

---

## 09.0 Data Storage

**Feature:** Local Room database for projects, photos, and annotations. Managed image file storage.
**Files:**
- `app/src/main/java/com/changelens/app/data/AppDatabase.java` — Room @Database (3 entities, version 1)
- `app/src/main/java/com/changelens/app/data/ProjectRepository.java` — Repository with disk-serialized operations
- `app/src/main/java/com/changelens/app/util/ImageStore.java` — Image/thumbnail/export/capture directory management
- `app/src/main/res/xml/file_paths.xml` — FileProvider paths for sharing

**Demonstration:** Create project → saved to Room. Add photos → copied to managed storage with thumbnails.
**Verification:** Data persists across app restart. Project creation tested on emulator.
**Status:** Production feature.

---

## 09.1 Shared Preferences

**Feature:** Settings persistence via SharedPreferences wrapper.
**Files:**
- `app/src/main/java/com/changelens/app/data/SettingsRepository.java` — SharedPreferences for language, theme, comparison mode, reminders, sort, onboarding
- `app/src/main/java/com/changelens/app/ui/SettingsActivity.java` — Settings UI reading/writing preferences
- `app/src/main/java/com/changelens/app/util/LocaleHelper.java` — Locale persistence via SettingsRepository

**Demonstration:** Change language in setting → persists. Kill app → relaunch → language still selected.
**Verification:** Language switching tested on emulator — Persian and Dari both persist after navigation.
**Status:** Production feature.

---

## 09.2 App settings

**Feature:** Functional settings for language, theme, comparison mode, reminders, storage info, and reset.
**Files:**
- `app/src/main/java/com/changelens/app/ui/SettingsActivity.java` — Language picker (en/fa-IR/fa-AF with restart), theme picker (system/light/dark), comparison mode, reminder switch + time picker, storage info, privacy text, reset with confirmation
- `app/src/main/res/layout/activity_settings.xml` — Settings layout
- `app/src/main/res/values/strings.xml` — All settings strings

**Demonstration:** Settings → Language → Persian → app restarts in Persian. Settings → Theme → Dark → dark theme applied. Settings → Reset → confirmation dialog → defaults restored.
**Verification:** All settings tested on emulator — language, theme, and reset confirmed.
**Status:** Production feature.

---

## 10.0 SQLite Primer

**Feature:** SQLite fundamentals via Room — entities, DAOs, queries, foreign keys, indexes, transactions.
**Files:**
- `app/src/main/java/com/changelens/app/data/entity/Project.java` — @Entity with indices
- `app/src/main/java/com/changelens/app/data/entity/PhotoEntry.java` — @Entity with foreign key CASCADE
- `app/src/main/java/com/changelens/app/data/entity/Annotation.java` — @Entity with foreign key CASCADE
- `app/src/main/java/com/changelens/app/data/dao/ProjectDao.java` — CRUD queries, sort modes, LiveData
- `app/src/main/java/com/changelens/app/data/dao/PhotoEntryDao.java` — Ordered queries, count, max display order
- `app/src/main/java/com/changelens/app/data/dao/AnnotationDao.java` — Per-photo annotation queries
- `app/src/main/java/com/changelens/app/data/AppDatabase.java` — @Database with 3 entities, destructive migration on downgrade

**Demonstration:** Create project → INSERT. View timeline → SELECT ORDER BY. Delete project → CASCADE deletes photos + annotations.
**Verification:** All CRUD operations tested. Foreign key CASCADE confirmed. `ProjectRepository.deleteProjectDeep()` handles file cleanup.
**Status:** Production feature.

---

## 10.1 Room, LiveData, and ViewModel

**Feature:** Full Room-backed persistence with DAOs, repositories, observable data, and ViewModels.
**Files:**
- `app/src/main/java/com/changelens/app/data/AppDatabase.java` — Room database singleton
- `app/src/main/java/com/changelens/app/data/ProjectRepository.java` — Repository with LiveData observables
- `app/src/main/java/com/changelens/app/data/dao/` — 3 DAOs with LiveData + blocking queries
- `app/src/main/java/com/changelens/app/ui/ProjectsViewModel.java` — AndroidViewModel with MediatorLiveData (search + filter + sort)
- `app/src/main/java/com/changelens/app/ui/MainActivity.java` — Observes ViewModel LiveData, updates UI reactively
- `app/src/main/java/com/changelens/app/ui/EditProjectActivity.java` — Inner EditViewModel survives rotation

**Demonstration:** Dashboard → type in search → list filters reactively. Change sort → list reorders. Rotate → ViewModel preserves state.
**Verification:** LiveData observation confirmed. Search/filter/sort tested. ViewModel survives rotation.
**Status:** Production feature.

---

## Summary

| # | Topic | Status | Type |
|---|-------|--------|------|
| 01.2 | Layouts and resources for the UI | Complete | Production |
| 01.3 | Text and scrolling views | Complete | Production |
| 01.4 | Resources to help you learn | Complete | Production |
| 02.1 | Activities and Intents | Complete | Production |
| 02.2 | Activity lifecycle and state | Complete | Production |
| 02.3 | Implicit Intents | Complete | Production |
| 03.1 | Android Studio debugger | Complete | Production |
| 03.2 | App testing | Complete | Production |
| 03.3 | Android Support Library | Complete | Production |
| 04.1 | Buttons and clickable images | Complete | Production |
| 04.2 | Input controls | Complete | Production |
| 04.4 | User navigation | Complete | Production |
| 04.5 | RecyclerView | Complete | Production |
| 05.1 | Drawables, styles, and themes | Complete | Production |
| 05.2 | Material Design | Complete | Production |
| 05.3 | Resources for adaptive layouts | Complete | Production |
| 06.1 | UI testing | Complete | Production |
| 07.1 | AsyncTask and AsyncTaskLoader | Complete | Educational |
| 07.2 | Internet connection | Complete | Production |
| 07.3 | Broadcasts | Complete | Production |
| 07.4 | Services | Complete | Production |
| 08.1 | Notifications | Complete | Production |
| 08.2 | Alarms | Complete | Production |
| 08.3 | Efficient data transfer and JobScheduler | Complete | Production |
| 09.0 | Data Storage | Complete | Production |
| 09.1 | Shared Preferences | Complete | Production |
| 09.2 | App settings | Complete | Production |
| 10.0 | SQLite Primer | Complete | Production |
| 10.1 | Room, LiveData, and ViewModel | Complete | Production |

**Total: 29/29 topics implemented and verified.**
