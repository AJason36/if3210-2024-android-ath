# BondoMan

BondoMan is an **Android Based Application** for _money management_ built using Kotlin, RoomDB, and Hilt Dependency Injection. This application is built to fulfill Tugas Besar Android for Platform Based Development IF3210 2023/2024. This app's minimum SDK version is Android API Level 29, with compile target Android API Level 34.

## Features
This app has a Login page to verify that only authenticated user can use the app.

This app is divided into 4 navigations:
**1. Transaction**
This page is for managing transactions. User can:
- See transaction list 
- Add new transaction with information including transaction name, amount, current location, and category (income/expenditure)
- Edit and delete transsaction

**2. Scan**
This page is for scanning a bill. The bill is scanned from phone's camera and then the picture will be sent to a backend server to get the scan result. The scan result will be shown and the transaction will be added to transaction list.

**3. Graph**
This page is to visualize the proportion between expenditures and income.

**4. Settings**
This page provides additional features:
- Send transactions to export user's transactions and send them to the user's email
- Randomize transactions to insert random transaction
- Export transactions to .xlsx
- Export transsactions to .xls

## Requirements

1. Android with minimum API SDK Level 29

## Screenshots

## Pembagian Tugas
**13521044**
- Setup repository and project
- Login page
- App navigation
- Background Service to Verify JWT
- Network Sensing


## Bonus
**OWASP**

## Authors

|            Nama            |   NIM    |
| :------------------------: | :------: |
| Rachel Gabriela Chen       | 13521044 |
| Jeffrey Chow               | 13521046 |
| Alexander Jason            | 13521100 |

