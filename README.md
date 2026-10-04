# RemindHub 🎯

> **Your unified daily thoughts, tasks, and finance tracker.**

RemindHub is a hybrid productivity ecosystem designed for modern day-to-day organization. Built with a unified Web application (SPA) and an Android WebView wrapper, RemindHub bridges responsive client-side web UX with native Android capabilities such as device-level alarm scheduling, notifications, and intent sharing.

---

## 📱 Features & Modules

RemindHub consolidates seven dedicated utility modules under a single dark-mode interface:

* **💭 Thoughts & Notes:** A digital notebook featuring lined-paper aesthetics, character counting, quick editing, and multi-channel sharing.
* **💊 Medicine Time:** Prescription and daily dosage management with time slots, meal instructions, and native audio alarm triggers.
* **✅ Task or Groceries:** Checklist with instant progress indicators, percentage pills, completed task cleanup, and local reset controls.
* **💳 Payment Reminders:** Dues and receivables tracker supporting inbound (`Receive`) and outbound (`Send`) balances with settlement tracking.
* **📈 Stock Market (Mon–Fri):** A Monday-to-Friday trading journal calculating daily gain/loss inputs into net weekly P&L summaries.
* **📍 Address Storage:** Structured storage for residential and delivery addresses with 1-tap clipboard copying and messaging export.
* **🏦 Bank Details:** Storage for account holders, bank names, account numbers, and IFSC codes with error-free sharing.
* **⚙️ App Preferences & Theming:** Custom color accents (Emerald, Slate Black, Rose Red, Orange), dark/light mode toggle, multilingual localization, and automated reminder schedules.

---

## 🏗️ Architecture & Tech Stack

### Web Application
* **Frontend:** HTML5, Tailwind CSS, Lucide Icons
* **Runtime / Client Logic:** Vanilla JavaScript (ES6+)
* **Authentication & Backend:** Firebase Authentication (Email/Password, Phone OTP, Google SSO, Apple Sign-In), Cloud Firestore
* **Internationalization (i18n):** Client-side dictionary supporting English (US/UK), Hindi, Marathi, Gujarati, Bengali, Telugu, and Tamil

### Android Application
* **Framework:** Android SDK (Java)
* **Core Components:**
  * `MainActivity.java`: Houses the `WebView` shell, injects JavaScript bridge interfaces, and intercepts native system back navigation.
  * `AlarmReceiver.java`: Handles `BroadcastReceiver` alarms for scheduled medication and reminder intents.
  * `AndroidManifest.xml`: Manages app permissions (Internet, Wake Lock, Exact Alarm scheduling, Boot Completed).

### UI/UX Design
* **Design Specs:** [Figma Design File](https://www.figma.com/design/riASVvLnBLLUg0wcIvw1QD/RemindHub?t=s1KDSzO5KDJOhn7j-0)

---

## 📂 Repository Structure

```text
RemindHub/
├── android/
│   ├── app/
│   │   └── src/
│   │       └── main/
│   │           ├── AndroidManifest.xml
│   │           └── java/com/remindhub/app/
│   │               ├── MainActivity.java
│   │               └── AlarmReceiver.java
├── web/
│   └── index.html             # Single-Page Web Application
├── design/
│   └── README.md              # Figma spec links, design tokens & screenshots
├── .gitignore
└── README.md