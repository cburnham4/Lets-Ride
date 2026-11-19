# Google Play Data Safety Responses

**App:** Let's Ride (`letshangllc.letsride`)  
**Version:** 3.0.0 (versionCode 100)  
**Last Updated:** November 2026  
**Target SDK:** 36

This document supports filling the Google Play Console Data safety form.

---

## Overview

Let's Ride is a local GPS ride tracker. Ride history (locations, speeds, elevations, durations) is stored in an on-device SQLite database. The app uses Google Maps to show ride paths, Google Play Location services for tracking, Google AdMob for banners, and Google UMP for consent.

---

## Data Collection Summary

| Data Type | Collected | Shared | Purpose |
|-----------|-----------|--------|---------|
| Precise location | Yes | No (app); Yes (Maps/Ads SDKs as applicable) | App functionality (ride tracking / map display) |
| Approximate location | Yes | Yes (AdMob / Maps) | App functionality, Advertising |
| App interactions | Yes | Yes (AdMob) | Advertising, Analytics |
| Advertising ID | Yes | Yes (AdMob) | Advertising |
| Crash logs / diagnostics | No (no Crashlytics in this app) | — | — |

---

## Detailed Data Types

### Location

- **Precise location:** Collected while the user records a ride (foreground). Stored locally in SQLite. Used to draw the path on Google Maps.
- **Approximate location:** May be used by Maps / AdMob SDKs.
- **Background location:** Not requested. Tracking runs while the Record Ride screen is active.
- **Encrypted in transit:** Yes (HTTPS) for SDK network calls.
- **User control:** Runtime location permission; user can deny or revoke in system settings.

### Device or other IDs

- **Advertising ID:** Collected/shared by Google AdMob when ads are served.
- **User control:** UMP consent (EEA/UK) and device advertising settings.

### App activity

- **App interactions:** Shared with AdMob for ad serving / personalization subject to consent.

---

## Third-Party SDKs

### Google Maps SDK for Android
- **Data:** Map tiles / location for displaying the ride path  
- **Privacy:** https://policies.google.com/privacy  
- **Note:** Owner must supply a Maps API key via `local.properties` (`MAPS_API_KEY=...`). Do not commit the key.

### Google Play services Location
- **Data:** Device location while recording  
- **Purpose:** Ride tracking  
- **Privacy:** https://policies.google.com/privacy

### Google AdMob (`play-services-ads`)
- **Data:** Advertising ID, app interactions, coarse location (as configured by Google)  
- **Purpose:** Banner ads  
- **Shared:** Yes, with Google advertising partners  
- **Consent:** UMP before requesting ads  
- **Privacy:** https://policies.google.com/privacy

### Google User Messaging Platform (UMP)
- **Purpose:** GDPR/consent management  
- **Privacy:** https://policies.google.com/privacy

---

## Permissions (Play declarations)

Declare / justify in Play Console as needed:

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` | Record ride GPS track |
| `ACCESS_COARSE_LOCATION` | Location fallback / ads & maps |
| `INTERNET` | Maps tiles, ads, consent |
| `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE` | Ads / connectivity checks |
| `POST_NOTIFICATIONS` | Android 13+; may be used by ads SDK |

**Sensitive / special:** Precise location — explain that it is used only for ride recording and map display, not sold by the app developer. No background location permission.

---

## Data Security

- **In transit:** HTTPS only (`usesCleartextTraffic=false`, network security config denies cleartext).
- **At rest:** Ride data stored in app-private SQLite.
- **Deletion:** Uninstalling the app removes local ride history. Users can also discard a recording before save.

---

## Children's Privacy

Not directed at children under 13 (COPPA) or under 16 (GDPR).  
`setTagForUnderAgeOfConsent(false)` is set in UMP. No child-directed ad serving is enabled.

---

## Play Console Form Shortcuts

- Collects/shares required user data types? **Yes**
- Encrypted in transit? **Yes**
- Users can request deletion? **Yes** (uninstall / discard; contact developer email on listing for further requests)
- Location collected? **Yes** (precise + approximate) — purpose: App functionality; shared with Maps/AdMob as applicable
- Device or other IDs? **Yes** — Advertising; shared with AdMob
- Families policy / ads: Ensure ad content and targeting comply if the listing is in any family program (this app is not intended as a children's app)
