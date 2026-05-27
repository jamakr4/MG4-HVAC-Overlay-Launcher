# MG4 HVAC Overlay Launcher

This mini-project launches the MG4 climate overlay view with the top close button.

## What the app does

The app is just a lightweight launcher:

- it binds to `com.android.systemui.StartActivityService`
- it uses the Binder interface `com.saicmotor.sdk.external.IPageService`
- it calls `openHvac()`

This means it does not launch the normal full-screen HVAC app, but instead triggers the `SystemUI`-owned HVAC overlay path.

## Normal OEM flow

On the MG4, the normal path to this overlay does not come directly from the HVAC app itself, but from `SystemUI`.

## Components involved

- Full HVAC app:
  - package: `com.saicmotor.hmi.hvac`
  - activity: `com.saicmotor.hmi.hvac.HvacActivity`

- Overlay controller:
  - package: `com.android.systemui`
  - service: `com.android.systemui.StartActivityService`
  - Binder implementation: `com.android.systemui.PageManagerBinder`

- Overlay view with close button:
  - `com.android.systemui.saicmotor.view.HVACPageView`
  - layout: `layout_hvac_page.xml`
  - top close / pickup button: `iv_pick_up`

## How the overlay is normally shown

The typical flow is:

1. An OEM component in `SystemUI`, or code using `PageManager`, requests `openHvac()`.
2. `PageManagerBinder.openHvac()` sets the HVAC start type to `0` (`HVAC_START_BY_HAND`).
3. `SystemUI` switches the status bar and dock into HVAC mode.
4. `SystemUI` shows the `HVACPageView` overlay.
5. That view contains the extra top close button.

This means:

- the normal HVAC app is not responsible for that close button
- the close button belongs to the `SystemUI` overlay view

## Difference from the normal HVAC app

There are two different HVAC presentations:

- `HvacActivity`
  - full climate app
  - no `SystemUI` overlay bar with the top close button

- `HVACPageView`
  - `SystemUI` overlay
  - includes the top close button
  - this is the exact view launched by this project

There is also a smaller HVAC info dialog in `SystemUI`:

- `HvacControlDialog`

However, that is not the large overlay view with the top close button.

## What this project recreates

This project only recreates the entry point:

- tap the app icon
- bind to `StartActivityService`
- call `openHvac()`
- `SystemUI` shows the HVAC overlay

## Signing

To integrate cleanly into the MG4 OEM environment, the APK should be signed with the same platform keys already used in the other project.

Existing key files:

- `platform.pk8`
- `platform.x509.pem`

This project does not copy those keys. Instead, the signing script directly references an existing directory.

If you want to build later, the intended flow is:

1. build the release APK
2. run `apksigner` using `platform.pk8` and `platform.x509.pem`
3. install the signed APK in the car

The included script for this is:

- `tools/build_sign_release.sh`

## Important note

This only works if the target system:

- exports `com.android.systemui.StartActivityService`
- still provides the same Binder interface
- does not block third-party apps from binding to that service

If the bind fails on a different software version, that is most likely an OEM / firmware difference rather than a bug in this app.
