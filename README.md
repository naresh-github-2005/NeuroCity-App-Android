
# 🏙️ **NeuroCity – Civic Issue Tracking & Resolution Platform**

> **Empowering citizens. Enhancing governance. Building smarter cities.**

<p align="center">
  <img src="assets/banner.png" alt="NeuroCity Banner" width="800"/>
</p>

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/Android-14%2B-green?logo=android&logoColor=white" alt="Android Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/Java-100%25-blue?logo=openjdk&logoColor=white" alt="Java Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/Firebase-Backend-orange?logo=firebase&logoColor=white" alt="Firebase Badge"></a>
  <a href="#"><img src="https://img.shields.io/badge/License-MIT-lightgrey?logo=opensourceinitiative&logoColor=white" alt="License Badge"></a>
</p>

---

## 📘 **Overview**

**NeuroCity** is a next-generation **Civic Issue Tracking and Resolution Android App** that bridges communication between **citizens** and **municipal authorities**.
It enables people to **report local problems** (like potholes, streetlight failures, or garbage accumulation) directly from their smartphones and allows **authorities** to **monitor, update, and resolve** these reports efficiently.

---

## 🧭 **Table of Contents**

* [Core Features](#-core-features)
* [User Roles](#-user-roles)
* [Application Workflow](#-application-workflow)
* [Technology Stack](#-technology-stack)
* [Android Components](#-android-components)
* [Project Architecture](#-project-architecture)
* [Firestore Database Structure](#-firestore-database-structure)
* [Permissions](#-permissions)
* [Setup & Configuration](#-setup--configuration)
* [Screenshots](#-screenshots)
* [Future Enhancements](#-future-enhancements)
* [Contributors](#-contributors)
* [License](#-license)

---

## ⚙️ **Core Features**

| Feature                         | Description                                                                  |
| ------------------------------- | ---------------------------------------------------------------------------- |
| 🔐 **Secure Authentication**    | Email/Password-based login & registration using Firebase Authentication.     |
| 📸 **Smart Issue Reporting**    | Capture or upload images, describe the issue, and geotag the exact location. |
| 🗺️ **Google Maps Integration** | Interactive maps to view nearby issues and locations.                        |
| ☁️ **Cloud Firestore Database** | Real-time data synchronization and dynamic issue listing.                    |
| 📰 **Live Feed**                | Issues automatically update in RecyclerView without app refresh.             |
| 🧑‍💼 **Admin Dashboard**       | Separate dashboard for admins to manage and update issue statuses.           |
| 🪶 **Material Design UI**       | Clean and minimal interface following Google Material 3 principles.          |

---

## 👥 **User Roles**

| Role                    | Description                                                                     |
| ----------------------- | ------------------------------------------------------------------------------- |
| 👤 **Citizen User**     | Register, log in, report issues, view feed and map, track issue status.         |
| 🧑‍💼 **Administrator** | View all reported issues, update statuses (Pending → Resolved), manage reports. |

---

## 🔄 **Application Workflow**

1. **Splash & Authentication**

    * Starts at `SplashActivity`
    * Directs to `LoginActivity` or `RegisterActivity`
2. **Main Interface (Citizen)**

    * `BottomNavigationView` → `ComplaintsFragment`, `UploadFragment`, `MapFragment`
3. **Admin Flow**

    * Role verified via Firebase (based on admin email)
    * Redirected to `AdminDashboardActivity`
4. **Real-time Sync**

    * All issues, updates, and images sync with Firestore instantly

---

## 🧰 **Technology Stack**

| Layer               | Technology Used                        |
| ------------------- | -------------------------------------- |
| **Language**        | Java                                   |
| **UI & Layouts**    | XML + Material Design Components       |
| **Database**        | Firebase Firestore                     |
| **Authentication**  | Firebase Auth (Email/Password)         |
| **Storage**         | Firebase Storage                       |
| **Maps & Location** | Google Maps SDK, FusedLocationProvider |
| **Image Handling**  | Glide                                  |
| **Navigation**      | Android Navigation Component           |
| **View Binding**    | Enabled for type-safe view access      |
| **Version Control** | Git & GitHub                           |
| **Build System**    | Gradle                                 |

---

## 🧱 **Android Components**

| Component                         | Type           | Purpose                                                         |
| --------------------------------- | -------------- | --------------------------------------------------------------- |
| **RecyclerView**                  | UI Widget      | Displays list of issues dynamically.                            |
| **CardView**                      | UI Widget      | Visual card for each issue (image, title, description, status). |
| **ImageView**                     | UI Widget      | Shows uploaded issue photo.                                     |
| **TextView**                      | UI Widget      | Displays titles, issue descriptions, status, and location.      |
| **EditText**                      | UI Widget      | Accepts user input (issue title, description, etc.).            |
| **Button**                        | UI Widget      | Handles actions like Submit, Login, Register, Upload.           |
| **FloatingActionButton**          | UI Widget      | Quick access to report a new issue (optional).                  |
| **ProgressBar**                   | UI Widget      | Indicates loading states (upload, authentication).              |
| **BottomNavigationView**          | Navigation     | Switches between fragments (Complaints, Upload, Map).           |
| **Fragment**                      | UI Module      | Modular screens within MainActivity.                            |
| **ConstraintLayout**              | Layout         | Defines structured UI layout with flexible positioning.         |
| **LinearLayout / RelativeLayout** | Layout         | Used for nested structure and orientation of child views.       |
| **Toolbar / AppBarLayout**        | UI Element     | Displays title and action icons at top.                         |
| **ViewBinding**                   | Utility        | Safely binds XML views to Java code.                            |
| **Intent / Bundle**               | Core Component | Transfers data between Activities.                              |
| **SharedPreferences**             | Storage        | Caches login or local configuration data.                       |
| **FirebaseAuth**                  | API            | Handles user login and registration.                            |
| **FirebaseFirestore**             | API            | Stores and retrieves civic issue data in real time.             |
| **FirebaseStorage**               | API            | Stores uploaded images.                                         |
| **FusedLocationProviderClient**   | API            | Gets device GPS coordinates.                                    |
| **Glide**                         | Library        | Loads and caches images efficiently.                            |

---

## 🏗️ **Project Architecture**

![img_6.png](img_6.png)

```
com.example.neurocity
│
├── activities/
│   ├── SplashActivity.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── MainActivity.java
│   ├── AdminDashboardActivity.java
│   └── SettingsActivity.java
│
├── fragments/
│   ├── ComplaintsFragment.java
│   ├── UploadFragment.java
│   └── MapFragment.java
│
├── adapters/
│   ├── ComplaintsAdapter.java
│   └── AdminIssuesAdapter.java
│
├── models/
│   └── CivicIssue.java
│
└── utils/
    └── FirebaseUtils.java
```

---

## 🗄️ **Firestore Database Structure**

![img_5.png](img_5.png)

```
Firestore Root
│
└── Collection: civic_issues
    ├── Document ID: <auto-generated>
    │   ├── title: "Pothole near School"
    │   ├── description: "Large pothole blocking road"
    │   ├── imageUrl: "https://firebasestorage.googleapis.com/..."
    │   ├── latitude: 19.12345
    │   ├── longitude: 72.54321
    │   ├── reporterEmail: "user@example.com"
    │   ├── timestamp: <server_timestamp>
    │   ├── status: "Pending" // or "In Progress", "Resolved"
    │   └── adminComment: "Resolved by Public Works Dept"
    │
    ├── Document ID: <auto-generated>
    │   └── ...
```

---

## 🔒 **Permissions**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

## ⚙️ **Setup & Configuration**

1. **Clone the Repository**

   ```bash
   git clone https://github.com/your-username/NeuroCity.git
   ```

2. **Open in Android Studio**

3. **Firebase Setup**

    * Add your Android app (`com.example.neurocity`)
    * Download and place `google-services.json` in `/app`
    * Enable:

        * 🔑 Authentication → Email/Password
        * 🔥 Firestore Database
        * 📦 Storage

4. **Google Maps API Key**

   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```

5. **Run the App**

    * Sync Gradle → Run on Emulator or Device

---

## 🖼️ **Screenshots**

| Splash | Login |       Report Issue       | Admin Dashboard |
|:------:|:-----:|:------------------------:|:---------------:|
|    ![img.png](img.png)    |   ![img_1.png](img_1.png)    | ![img_2.png](img_2.png) ![img_3.png](img_3.png) |        ![img_4.png](img_4.png)         |

---

## 🚀 **Future Enhancements**

* 🟢 **Status Updates:** Admins update issue lifecycle.
* 🔔 **Push Notifications:** Notify users when issue status changes.
* 👤 **User Profiles:** View report history.
* 📊 **Analytics Dashboard:** Visualize civic data.
* 📶 **Offline Mode:** Local caching for low-connectivity areas.
* 🗂️ **Advanced Filtering:** Filter by category, date, or status.

---

## 👨‍💻 **Contributors**

| Name                  | Contact                                                                               |
|-----------------------|---------------------------------------------------------------------------------------|
| **Mohammed Arfath R** | [mohammedarfath.r2022@vitstudent.ac.in](mailto:mohammedarfath.r2022@vitstudent.ac.in) |
| **Naresh R**          | [naresh.r2022a@vitstudent.ac.in](mailto:naresh.r2022a@vitstudent.ac.in)               |
| **Mohammad Yusuf**    | [mohammadyusuf.ka2022@vitstudent.ac.in](mailto:mohammadyusuf.ka2022@vitstudent.ac.in) |

---

## 📜 **License**

Licensed under the **MIT License**.
See the [LICENSE](LICENSE) file for full text.

---

## 💡 **Project Vision**

> *NeuroCity* aims to revolutionize civic engagement through **data-driven smart governance**, enabling every citizen to contribute towards building cleaner, safer, and smarter cities.

---
