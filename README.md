
# 🏙️ **NeuroCity – AI-Powered Civic Issue Tracking & Resolution Platform**

> **Empowering Citizens. Enhancing Governance. Building Smarter Cities.**

<p align="center">
  <img width="1333" height="411" alt="NeuroCity Banner" src="https://github.com/user-attachments/assets/ee7387e9-290b-429e-8d87-dde2f8ff1702" />
</p>

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/Android-14%2B-green?logo=android&logoColor=white" alt="Android Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/Java-100%25-blue?logo=openjdk&logoColor=white" alt="Java Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/Firebase-Backend-orange?logo=firebase&logoColor=white" alt="Firebase Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/Google%20Maps-Integrated-blue?logo=googlemaps&logoColor=white" alt="Maps Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/License-MIT-lightgrey?logo=opensourceinitiative&logoColor=white" alt="License Badge"></a>
</p>

---

## 📘 **Overview**

**NeuroCity** is an **AI-integrated civic management Android application** that connects **citizens**, **field workers**, and **administrators** on one cloud-powered platform.  
It enables citizens to **report urban issues**, helps workers **resolve tasks efficiently**, and provides administrators with **real-time oversight** using Firebase’s live synchronization and FCM push notifications.

---

## 🧩 **Problem Statement**

Modern cities face numerous civic issues — from potholes and waste overflow to damaged streetlights — yet most platforms lack:
- AI-driven classification or prioritization.
- Real-time updates between citizens, workers, and admins.
- Role-based dashboards for clarity and accountability.

**NeuroCity** solves this by integrating **Firebase Cloud**, **AI-assisted workflows**, and **role-based access control** in a single mobile solution.

---

## 🧭 **Table of Contents**

- [Core Features](#-core-features)
- [User Roles](#-user-roles)
- [Application Workflow](#-application-workflow)
- [Technology Stack](#-technology-stack)
- [Android Components](#-android-components)
- [Firebase Integration](#-firebase-integration)
- [UI/UX Design Principles](#-uiux-design-principles)
- [Project Architecture](#-project-architecture)
- [Firestore Database Structure](#-firestore-database-structure)
- [Permissions](#-permissions)
- [Setup & Configuration](#-setup--configuration)
- [Screenshots](#-screenshots)
- [Future Enhancements](#-future-enhancements)
- [Contributors](#-contributors)
- [License](#-license)

---

## ⚙️ **Core Features**

| Feature | Description |
|----------|--------------|
| 🔐 **Secure Authentication** | Email/Password & Google Sign-In via Firebase Authentication. |
| 📸 **Smart Issue Reporting** | Capture or upload images, select issue type, and tag GPS location. |
| 🗺️ **Google Maps Integration** | Interactive civic map with live issue markers. |
| ☁️ **Cloud Firestore Database** | Real-time issue storage, queries, and snapshot listeners. |
| 🧠 **Role-Based Dashboards** | Separate UI flows for Citizen, Worker, and Admin users. |
| 🔔 **Push Notifications** | FCM-powered real-time updates (e.g., issue assigned/resolved). |
| 🎨 **Material Design UI** | Minimal, consistent interface following Material 3 principles. |
| 🌗 **Theme Preferences** | Light, Dark, or System Default modes with persistent settings. |

---

## 👥 **User Roles**

| Role | Description |
|------|--------------|
| 👤 **Citizen** | Reports civic issues, uploads photos, views status, and tracks progress. |
| 👷 **Worker** | Accesses assigned issues, uploads resolution photos, and generates routes via Google Maps. |
| 🧑‍💼 **Admin** | Monitors all issues, filters by status, assigns workers, and updates progress in real time. |

---

## 🔄 **Application Workflow**

1. **Splash & Authentication**
   - Launches from `SplashActivity`
   - Redirects to `LoginActivity` or `RegisterActivity` (auto-login enabled)

2. **Citizen Flow**
   - `MainActivity` hosts `UploadFragment`, `MapFragment`, `ComplaintsFragment`
   - Citizens can report new issues, view existing ones, and track progress
  
    <img width="519" height="907" alt="Untitled diagram-2025-11-02-145107" src="https://github.com/user-attachments/assets/81598b5f-2a6b-4a65-bddb-593b5660f411" />



3. **Worker Flow**
   - `WorkerDashboardActivity` lists assigned issues
   - Workers can upload resolution photos and generate optimized routes
     
<img width="248" height="1139" alt="Untitled diagram-2025-11-02-144748" src="https://github.com/user-attachments/assets/396b29de-f60c-4277-a6ac-1a54008999d9" />



4. **Admin Flow**
   - `AdminDashboardActivity` shows all civic issues in real time
   - Admins can filter, assign, and update statuses instantly

  <img width="554" height="907" alt="Untitled diagram-2025-11-02-145004" src="https://github.com/user-attachments/assets/460cd765-07cb-4552-beaf-f74da0bfef05" />



5. **Notification Flow**
   - FCM sends targeted notifications for updates and assignments
  
  <img width="1037" height="183" alt="Untitled diagram-2025-11-02-144554" src="https://github.com/user-attachments/assets/4edcbf89-4ee5-4ff4-8b21-feb3c70d754a" />



---

## 🧰 **Technology Stack**

| Layer | Technology Used |
|--------|-----------------|
| **Frontend** | Android (Java + XML) |
| **Backend** | Firebase (Firestore, Auth, Storage, Cloud Messaging) |
| **UI Design** | Material Design 3 |
| **Location Services** | Google Maps SDK + FusedLocationProvider |
| **Image Handling** | Glide |
| **Architecture** | MVVM + Single Activity (with Fragments) |
| **Build System** | Gradle |
| **Version Control** | Git & GitHub |

---

## 🧱 **Android Components**

| Component | Type | Function |
|------------|-------|-----------|
| `RecyclerView`, `CardView` | UI Widgets | Display civic issues dynamically. |
| `TextInputLayout`, `MaterialButton`, `ProgressBar` | Input & Feedback | Handle user input and state changes. |
| `BottomNavigationView` | Navigation | Switches between core fragments. |
| `Fragment` | Module | Modular views for Upload, Map, and Complaints. |
| `FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage` | API | Authentication, Data, and Media storage. |
| `SharedPreferences` | Local Storage | Save user preferences (theme, login). |
| `FloatingActionButton` | Action | Quick route generation (Worker). |
| `NotificationManager` | System Service | Builds and displays FCM notifications. |

---

## ☁️ **Firebase Integration**

| Service | Purpose |
|----------|----------|
| **Firebase Authentication** | User login, registration, and Google Sign-In. |
| **Cloud Firestore** | Stores user profiles, issues, and role data in real time. |
| **Firebase Storage** | Uploads and retrieves issue and resolution images. |
| **Firebase Cloud Messaging (FCM)** | Sends notifications for issue updates and worker assignments. |
| **Realtime Snapshot Listeners** | Sync UI with Firestore data changes instantly. |

---

## 🎨 **UI/UX Design Principles**

| Principle | Implementation |
|------------|----------------|
| **Consistency** | Shared Material Design components across all screens. |
| **Clarity** | Inline validation, helper texts, and progress bars. |
| **Feedback** | Toasts, snackbars, and real-time state color updates. |
| **User Control** | Role-based navigation and easy logout or theme control. |
| **Relevance** | Worker and Admin see only assigned or authorized data. |
| **Accessibility** | ScrollViews, large buttons, and adaptive layouts. |
| **Personalization** | Light/Dark/System mode saved using `SharedPreferences`. |

---

## 🏗️ **Project Architecture**

<img width="688" height="1189" alt="class_diagram" src="https://github.com/user-attachments/assets/eeee79ba-ea4d-421b-9bdf-0f30137b909f" />

```
com.example.neurocity
│
├── activities/
│   ├── SplashActivity.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── MainActivity.java
│   ├── AdminDashboardActivity.java
│   ├── WorkerDashboardActivity.java
│   └── SettingsActivity.java
│
├── fragments/
│   ├── UploadFragment.java
│   ├── MapFragment.java
│   ├── ComplaintsFragment.java
│
├── adapters/
│   ├── AdminIssuesAdapter.java
│   ├── WorkerIssuesAdapter.java
│   └── ComplaintsAdapter.java
│
├── models/
│   └── CivicIssue.java
│
└── services/
└── MyFirebaseMessagingService.java

```

---

## 🗄️ **Firestore Database Structure**

<img width="1030" height="1499" alt="Untitled diagram-2025-11-02-144349" src="https://github.com/user-attachments/assets/a1203d9f-1213-4545-a37a-426564e06373" />


```

Firestore Root
│
└── Collection: civic_issues
├── Document ID: <auto-generated>
│   ├── title: "Pothole near School"
│   ├── description: "Large pothole blocking road"
│   ├── image_url: "[https://firebasestorage.googleapis.com/](https://firebasestorage.googleapis.com/)..."
│   ├── latitude: 19.12345
│   ├── longitude: 72.54321
│   ├── user_id: "uid123"
│   ├── status: "Pending"
│   ├── assigned_worker_name: "John"
│   ├── resolved_image_url: ""
│   └── timestamp: <server_timestamp>

```

---

## 🛡️ **Permissions**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
````

---

## ⚙️ **Setup & Configuration**

1. **Clone the Repository**

   ```bash
   git clone https://github.com/Mohammed0Arfath/NeuroCity-App.git
   ```

2. **Add Firebase Config**

   * Place your `google-services.json` inside `/app`
   * Enable:

     * Authentication (Email/Password)
     * Firestore Database
     * Storage
     * Cloud Messaging

3. **Add Google Maps API Key**

   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```

4. **Build & Run**

   * Sync Gradle and launch on emulator/device.

---

## 🖼️ **Screenshots**

<p align="center"><em>Experience NeuroCity – Smart Civic Collaboration in Action</em></p>

### 🚀 **Splash & Authentication**

| Splash                  | Login                   | Register                |
| ----------------------- | ----------------------- | ----------------------- |
| *(Add screenshot here)* | *(Add screenshot here)* | *(Add screenshot here)* |

### 👤 **Citizen Side**

| Report Issue                      | Track Issue                              | View Map                       |
| --------------------------------- | ---------------------------------------- | ------------------------------ |
| *(Add UploadFragment screenshot)* | *(Add IssueTrackingActivity screenshot)* | *(Add MapFragment screenshot)* |

### 👷 **Worker Side**

| Worker Dashboard                           | Upload Resolved Image                | Route Generation             |
| ------------------------------------------ | ------------------------------------ | ---------------------------- |
| *(Add WorkerDashboardActivity screenshot)* | *(Add Worker Issue card screenshot)* | *(Add Route FAB screenshot)* |

### 🧑‍💼 **Admin Side**

| Admin Dashboard                    | Status Filter                     | Issue Management                 |
| ---------------------------------- | --------------------------------- | -------------------------------- |
| *(Add Admin Dashboard screenshot)* | *(Add Filter Buttons screenshot)* | *(Add Status Dialog screenshot)* |

---

## 🚀 **Future Enhancements**

* 🤖 **AI-Powered Classification:** Auto-tag issue type using computer vision.
* 🔔 **Push Notifications:** Real-time updates for citizens and workers.
* 📊 **Analytics Dashboard:** Visualize issue density and resolution metrics.
* 🗂️ **Advanced Filters:** Sort by area, category, or severity.
* 📶 **Offline Mode:** Local caching for low-connectivity regions.

---

## 👨‍💻 **Contributors**

| Name                  | Contact                                                                               |
| --------------------- | ------------------------------------------------------------------------------------- |
| **Naresh R**          | [naresh.r2022a@vitstudent.ac.in](mailto:naresh.r2022a@vitstudent.ac.in)               |
| **Mohammed Arfath R** | [mohammedarfath.r2022@vitstudent.ac.in](mailto:mohammedarfath.r2022@vitstudent.ac.in) |
| **Mohammad Yusuf**    | [mohammadyusuf.ka2022@vitstudent.ac.in](mailto:mohammadyusuf.ka2022@vitstudent.ac.in) |

---

## 📜 **License**

Licensed under the **MIT License**.
See the [LICENSE](LICENSE) file for more details.

---

## 💡 **Project Vision**

> *NeuroCity* redefines civic engagement through **AI-driven issue tracking**, **real-time collaboration**, and **transparent governance**, empowering every citizen to help build cleaner, smarter cities.
