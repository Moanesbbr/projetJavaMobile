# PolySciTracker - Scientific Event Management

## Overview
PolySciTracker is an Android mobile application designed to help users track and manage scientific events such as conferences, seminars, and workshops. The application focuses on tracking submission deadlines, helping researchers and academics manage their event participation effectively.

## Features

### Core Features
- **Event Management:** Add, view, and edit scientific events with details including name, location, date, organizer, theme, and submission deadline.
- **Deadline-focused Sorting:** Events are automatically sorted by submission deadline with the nearest deadlines appearing first.
- **Visual Deadline Indicators:** Color-coded visual indicators show the status of deadlines (normal, approaching, urgent, passed).
- **Offline Functionality:** All data is stored locally on the device using SQLite, no internet connection required.

### Key Event Information
Each scientific event includes:
- Event name (conference, seminar, etc.)
- Location
- Event date
- Organizing institution (university, lab)
- Event theme/topic
- Paper submission deadline

## Technical Implementation

### Architecture
- **Language:** Java
- **Database:** SQLite for local data storage
- **UI Components:** RecyclerView, Material Design components
- **Design Pattern:** Follows recommended Android architecture patterns

### Key Components
- **Activities:** 
  - MainActivity - Displays the list of events
  - AddEditEventActivity - Handles adding new events and editing existing events
- **Adapters:** 
  - EventAdapter - Populates and manages the RecyclerView
- **Database:** 
  - SQLite implementation with DatabaseHelper
  - Event model and database contract

## Setup and Installation

### Prerequisites
- Android Studio 4.0+
- Minimum SDK: API 21 (Android 5.0 Lollipop)
- Target SDK: API 33 (Android 13)

### Building the Project
1. Clone the repository:
   ```
   git clone https://github.com/yourusername/PolySciTracker.git
   ```
2. Open the project in Android Studio
3. Build the project using the "Build" menu
4. Run on an emulator or physical device using the "Run" button

## Usage

### Adding a New Event
1. Tap the "+" floating action button in the main screen
2. Fill in all required event details
3. Tap "Save" to add the event to your list

### Editing Event Details
1. Tap on the event you want to edit in the list
2. Modify any details as needed
3. Tap "Save" to update the event

### Editing Just the Submission Deadline
1. Tap the "Edit" button on an event card in the list
2. Update the submission deadline
3. Tap "Save" to update only the deadline

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements
This project was created as part of the Android programming course assignment "Application de Gestion des Événements Scientifiques".
