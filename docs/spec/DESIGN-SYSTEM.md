# Design System — SAND WORKS

Status: **SPECIFIED.** Professional, production-grade. Brand direction: industrial, premium, modern, clean, powerful, trustworthy, operational. Uses the approved PNG brand assets only (no regeneration/redraw/SVG).

## 1. Colour tokens (Material 3 colour roles; exact hex in a theme tokens doc during implementation)
Recommendation (PROPOSED — owner approves exact palette in an ADR):
- **Primary (industrial amber/gold)** signalling energy/earnings: e.g. amber `#F5A623`-family.
- **Secondary/Neutral (graphite/stone)** for surfaces: near-black `#1E1E1E`, concrete greys.
- **Semantic:** success green (accrued/working), error red (warning/absent/error), warning amber.
Final tokens: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondary`, `surface`, `surfaceVariant`, `background`, `error`, `outline`, and dark-theme variants. Contrast meets AA at minimum (see ACCESSIBILITY.md).

## 2. Typography
- Typeface: system default (Roboto on Android) for reliability; scale uses Material 3 type roles (display/headline/title/body/label). Numeric figures use tabular where aligned (money/dates). Money formatted from paise at UI.

## 3. Spacing & grid
- 4dp base spacing scale (4/8/12/16/24/32). 8dp grid for layout. Standard content max width; responsive.

## 4. Elevation / borders / corners / surfaces
- Material 3 elevation tonal or shadow tokens; cards on `surfaceContainerLow`; subtle borders; radius scale 8/12/16/28. Avoid flat generic template look.

## 5. Components
Buttons (filled/tonal/outlined/text + destructive), cards, forms & fields (outlined, error, char counters), dialogs (confirm, destructive), bottom sheets, navigation (bottom bar ≤5, top app bar, drawer), badges, chips (filter), tables/lists, avatars (with profile photo fallback initial), icons (Material icons, consistent stroke), charts (simple accrual/day bars — only where useful), leaderboard components (rank rows), notification components, loading indicators (linear/spinner/skeleton), empty-state and error-state visuals (clear illustration + action, no fabricated images).

## 6. Dark & light theme
Both supported; tokens adapt; content contrast maintained in both. (Dark/light default = PROPOSED, owner may choose; implement both per DESIGN tokens.)

## 7. Motion & haptics
- Motion: short, purposeful (e.g. 150–300 ms) for navigation, feedback; respect reduced-motion (see ACCESSIBILITY.md).
- Haptics: light on confirm, warning/error on failure, only where meaningful; alternatives for reduced-motion/haptics-off.

## 8. Responsive layouts
- Phones primary. Large screens/tablets: use wider content, two-pane where sensible (e.g. owner trips list+detail). Touch targets ≥48dp. Text scaling supported.

## 9. Accessibility & content
- Semantics, content descriptions, contrast, colour-independent status. Details in ACCESSIBILITY.md.

## 10. Brand application
- Splash/About/auth welcome use the locked logo from `assets/sand_works_brand_assets`. No placeholder logos anywhere.
