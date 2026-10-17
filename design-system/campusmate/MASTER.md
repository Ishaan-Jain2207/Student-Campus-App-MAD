# CampusMate visual system

CampusMate is a student-focused campus guide and personal academic tool for NMIMS Chandigarh. The interface uses a calm, editorial light palette and native Android Views. This is the visual source of truth for the app and its Figma screens.

## Product rules

- Keep the app light-only, including when Android is set to dark mode.
- Use the attached attendance image as a reference for direct per-subject plus/minus controls and semester grouping. Do not copy its palette, typography, or desktop card grid.
- Attendance is a student-maintained personal record, editable by semester. It is not an NMIMS attendance feed or official record. Show this clearly wherever attendance appears.
- Empty data remains empty. Do not add example subjects, counts, students, events, tasks, or success states to the running app.
- Use Roboto, native Material components, and the existing Java/XML stack. Do not migrate to Compose to achieve this redesign.

## Token layers

### Primitive values

| Token | Value | Purpose |
|---|---|---|
| Warm paper | `#F7F5EF` | Main screen canvas |
| Warm white | `#FFFEFA` | Cards, sheets, and fields |
| Evergreen ink | `#18231F` | Primary text |
| Muted slate | `#66716B` | Supporting text |
| Moss | `#365942` | Primary actions and attended state |
| Deep persimmon | `#A94327` | Missed/destructive-adjacent accent |
| Soft moss | `#E7EFE7` | Positive status and Attend controls |
| Soft persimmon | `#FBECE5` | Miss controls |
| Warning amber | `#8A4B08` | Below-target advisory |
| Warm border | `#DDDCD3` | Dividers and outlines |

### Semantic aliases

- `canvas` → warm paper.
- `surface` → warm white.
- `text.primary` → evergreen ink; `text.secondary` → muted slate.
- `action.primary` → moss; `action.secondary` → deep persimmon.
- `attendance.present` → moss over soft moss.
- `attendance.missed` → deep persimmon over soft persimmon.
- `attendance.warning` → warning amber over warning-amber soft.
- `border.subtle` → warm border.

These values are represented by `colors.xml`, `themes.xml`, `dimens.xml`, reusable drawables, and the Figma token collection.

### Component rules

- Primary button: moss fill, warm-white label, 48dp or taller.
- Outlined action: warm-white fill, moss border and label, 48dp or taller.
- Surface card: warm white, 1dp warm border, 16dp corners, 16dp internal padding.
- Text fields/selectors: warm white with a 1dp warm border and visible focused/error states.
- Attendance session controls: two separate segmented groups per subject (`Attend −/+` and `Miss −/+`); each segment is at least 48dp tall and has an explicit screen-reader label. `+` adds one dated entry; `−` confirms removal of the latest matching entry.
- Use color together with words and counts; do not rely on color alone for attendance status.

## Layout and accessibility

- Use the 4, 8, 12, 16, 20, 24, 32dp spacing scale.
- Body copy is 16sp; captions are 13sp; titles remain clear and wrap instead of clipping.
- Keep at most five top-level destinations in bottom navigation. Home, Planner, Attendance, Campus, and Profile are direct destinations; secondary tools remain in the drawer.
- Respect large system font settings, keyboard resize, narrow screens, and screen-reader labels. Use a list for long data and preserve intentional empty states.
- Keep iconography vector-based and consistent. Do not use emoji as icons.

## Design source

Editable Android screen suite and tokens: [CampusMate Figma](https://www.figma.com/design/ouZsqimZfYoZj9cCyF5PiI).
