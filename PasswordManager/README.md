# Password Manager — Java OOP Capstone Project

A simple, secure, and user-friendly desktop Password Manager built with Java Swing.

---

## Project Structure

```
PasswordManager/
├── src/
│   ├── interfaces/
│   │   ├── Searchable.java       → Interface for keyword search
│   │   ├── Encryptable.java      → Interface for AES encrypt/decrypt
│   │   └── Exportable.java       → Interface for CSV export
│   ├── model/
│   │   ├── Account.java          → Abstract base class (OOP core)
│   │   ├── SocialAccount.java    → Subclass for social media accounts
│   │   ├── BankingAccount.java   → Subclass for banking accounts
│   │   ├── EmailAccount.java     → Subclass for email accounts
│   │   ├── WorkAccount.java      → Subclass for work accounts
│   │   └── AccountNotFoundException.java → Custom exception
│   ├── util/
│   │   ├── PasswordStrengthChecker.java  → Checks password strength
│   │   ├── PasswordGenerator.java        → Generates strong passwords
│   │   ├── AutoLockTimer.java            → Auto-lock after inactivity
│   │   └── BackupManager.java            → Creates backup files
│   ├── core/
│   │   ├── UserManager.java      → Handles registration & login
│   │   └── PasswordManager.java  → Main logic (add, delete, edit, save)
│   └── gui/
│       └── PasswordManagerGUI.java → Full Swing GUI
├── data/                         → Created automatically on first run
├── backups/                      → Backup files stored here
├── build.bat                     → Windows build & run script
├── build.sh                      → Linux/Mac build & run script
└── README.md
```

---

## OOP Concepts Used

| Concept         | Where Used |
|----------------|-----------|
| **Abstraction** | `Account` is abstract; subclasses provide `getType()` |
| **Encapsulation** | All fields are private with getters/setters |
| **Inheritance** | `SocialAccount`, `BankingAccount`, `EmailAccount`, `WorkAccount` extend `Account` |
| **Polymorphism** | `ArrayList<Account>` holds any subclass; `getType()` returns different values |
| **Interfaces** | `Searchable`, `Encryptable`, `Exportable` |
| **Custom Exception** | `AccountNotFoundException` |

---

## Features

- **Register & Login** — Multiple users, SHA-256 hashed passwords
- **Add Accounts** — Social Media, Banking, Email, Work
- **Edit Password** — Updates password with history tracking (last 5)
- **Delete Account** — With confirmation dialog
- **Search** — By website, username, or tag
- **Filter** — By account type (sidebar)
- **Sort** — By type or website
- **Password Strength** — Live indicator (Weak / Moderate / Strong / Very Strong)
- **Password Generator** — Adjustable length slider
- **Show/Hide Passwords** — Toggle in the table
- **Export CSV** — Exports all accounts to `export.csv`
- **Backup** — Saves encrypted backup to `backups/` folder
- **Auto-Lock** — Logs out after 5 minutes of inactivity
- **Stats** — Shows account breakdown and weak password count

---

## How to Run

### Requirements
- Java 11 or higher installed
- Run `java -version` in terminal to check

### Windows
```
Double-click build.bat
```
or in Command Prompt:
```
build.bat
```

### Linux / Mac
```bash
chmod +x build.sh
./build.sh
```

---

## Security Notes

- User passwords are hashed with **SHA-256** before storing
- Account passwords are encrypted with **AES** before saving to file
- Plain text passwords are never written to disk
