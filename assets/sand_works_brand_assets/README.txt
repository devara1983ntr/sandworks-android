SAND WORKS — LOCKED BRAND ASSETS
=================================

Source artwork:
- master/sand_works_app_icon_master.png = FIRST uploaded image; locked square app icon.
- master/sand_works_logo_master.png = SECOND uploaded image; locked horizontal brand logo.

IMPORTANT:
- Do not redraw, regenerate, trace, vectorize, recolor, or replace the artwork.
- Do not create SVG versions.
- Resizing/cropping for platform requirements is allowed only when the supplied artwork remains visually unchanged.
- Treat the two master PNGs as immutable source assets.

Android:
- android/mipmap-*/ic_launcher.png = density-sized launcher PNGs.
- android/play/sand_works_icon_512.png = 512px icon.
- android/play/sand_works_icon_1024.png = high-resolution working copy.

Brand logo:
- logo/ = resized copies of the supplied logo for UI, splash, About, documentation, etc.

Recommended package/application ID:
com.roshan.sandworks

Recommended app display name:
SAND WORKS

Signing:
- Debug builds: standard Android debug signing only.
- Private release APK: generate a dedicated release keystore locally; never commit it.
- Keep passwords outside Gradle/source control (environment variables or ignored local properties).
- Keep secure offline backups of the release keystore and credentials.
- Do not reuse the previously tracked/legacy keystore from the Flutter project.
