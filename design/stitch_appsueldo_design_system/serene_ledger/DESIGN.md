---
name: Serene Ledger
colors:
  surface: '#eefcff'
  surface-dim: '#c6dee3'
  surface-bright: '#eefcff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#dff8fd'
  surface-container: '#d9f2f7'
  surface-container-high: '#d4edf2'
  surface-container-highest: '#cee7ec'
  on-surface: '#071f23'
  on-surface-variant: '#404848'
  inverse-surface: '#1d3438'
  inverse-on-surface: '#dcf5fa'
  outline: '#717978'
  outline-variant: '#c0c8c8'
  surface-tint: '#3b6566'
  primary: '#002627'
  on-primary: '#ffffff'
  primary-container: '#0f3d3e'
  on-primary-container: '#7da8a8'
  inverse-primary: '#a3cfcf'
  secondary: '#2f6a3e'
  on-secondary: '#ffffff'
  secondary-container: '#b2f2ba'
  on-secondary-container: '#367044'
  tertiary: '#321d00'
  on-tertiary: '#ffffff'
  tertiary-container: '#4e3100'
  on-tertiary-container: '#d29437'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#beebeb'
  primary-fixed-dim: '#a3cfcf'
  on-primary-fixed: '#002020'
  on-primary-fixed-variant: '#224d4e'
  secondary-fixed: '#b2f2ba'
  secondary-fixed-dim: '#97d5a0'
  on-secondary-fixed: '#00210b'
  on-secondary-fixed-variant: '#145128'
  tertiary-fixed: '#ffddb4'
  tertiary-fixed-dim: '#fdba5a'
  on-tertiary-fixed: '#291800'
  on-tertiary-fixed-variant: '#633f00'
  background: '#eefcff'
  on-background: '#071f23'
  surface-variant: '#cee7ec'
  warm-canvas: '#F7F3EA'
  soft-card: '#FFFCF6'
  muted-surface: '#EFE7D8'
  muted-coral: '#C96B5A'
  soft-coral-bg: '#F6DCD6'
  mint-bg: '#E6F1E8'
  amber-bg: '#F8E7C6'
  border-soft: '#E3D8C8'
  border-strong: '#C9BCA8'
  text-secondary: '#526365'
  text-muted: '#7A898B'
typography:
  display-lg:
    fontFamily: Geist
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 48px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Geist
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: '0'
  stat-lg:
    fontFamily: Geist
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.02em
  stat-lg-mobile:
    fontFamily: Geist
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  body-base:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: '0'
  body-bold:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: '0'
  label-caps:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  container-max: 1280px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style

The design system is built on a **Warm & Editorial** philosophy, specifically tailored to alleviate the anxiety typically associated with personal finance. It departs from the "traditional bank" aesthetic—characterized by clinical blues and cold whites—and the "crypto" look of dark modes and neon accents. 

Instead, it draws inspiration from **Minimalism** and **Tactile** design movements. The UI should feel like a physical, high-quality paper report: warm, grounding, and organized. Every element is designed to evoke a sense of "control and relief" (calm, human, and trustworthy). 

**Key Brand Pillars:**
- **Explicado sin vueltas:** Direct, straightforward communication.
- **Human over Financial:** Prefer natural language ("Te queda para el mes") over technical jargon ("Net Disposable Income").
- **Physicality:** Use soft off-white surfaces and subtle hairline borders to create a layered, tangible feel without heavy skeuomorphism.

## Colors

The palette is rooted in organic, earthy tones. The background strategy is critical: **never use pure white** for the main canvas.

- **Background Strategy:** The primary page background is `warm-canvas` (#F7F3EA). UI containers and cards use `soft-card` (#FFFCF6) to create a gentle, paper-like lift. 
- **Typography:** Use the deep navy/teal `neutral` (#132A2E) for primary text. This provides high legibility while remaining warmer than pure black.
- **Semantic Accents:**
  - **Growth/Income:** `secondary` (#4F8A5B) paired with `mint-bg`.
  - **Expenses:** `muted-coral` (#C96B5A) paired with `soft-coral-bg`.
  - **Warnings/Alerts:** `tertiary` (#D99A3D) paired with `amber-bg`.
- **Interactive:** Use `primary` (#0F3D3E) for high-priority buttons and active navigation states.

## Typography

This design system uses a modern, technical, yet highly legible font stack that maintains an editorial feel through generous scale and precise weights.

**Key Principles:**
- **Editorial Presence:** Large headings use `display-lg` to set a confident tone.
- **Financial Clarity:** Financial figures use `stat-lg`. **Important:** Money must always be formatted in Chilean Pesos (CLP) without decimals (e.g., $850.000).
- **Sentence Case:** All UI labels and headers must use sentence case (e.g., "Últimos movimientos") to keep the tone friendly and human. Avoid all-caps except for specific `label-caps` usage in badges or timestamps.
- **Mobile Scaling:** Scale down large financial totals using `stat-lg-mobile` to ensure important balances don't wrap on small devices.

## Layout & Spacing

The system uses a **Fluid-Fixed hybrid grid** designed for a mobile-first experience that breathes on desktop.

- **Grid Model:** Use a 12-column grid for desktop with a maximum container width of `1280px`. On mobile, layouts should stack vertically with `16px` side margins.
- **Spacing Rhythm:** Based on a `4px` unit. Dashboards should feel airy; use `24px` (lg) or `32px` (xl) between major sections to prevent information density overload.
- **Structure:**
  - **Mobile:** Single column focus. The main balance card must occupy the primary visual slot.
  - **Desktop:** Sidebar-driven navigation with content centered. Use asymmetrical grids (e.g., a 2/3 width transaction list and a 1/3 width category breakdown).

## Elevation & Depth

Hierarchy is established through **Tonal Layering** and **Ambient Shadows** rather than high-contrast shadows or vibrant glows.

- **Stacking:**
  - **Level 0 (Canvas):** `#F7F3EA` (Warm Canvas).
  - **Level 1 (Cards):** `#FFFCF6` (Soft Card) with a 1px border of `#E3D8C8`.
  - **Level 2 (Popovers/Active Elements):** `#FFFFFF` (Elevated Surface) with a soft, diffused shadow (Blur: 12px, Y: 4px, Opacity: 5%, Tint: Primary Navy).
- **Outlines:** Use `border-soft` for standard separation. Use `border-strong` (#C9BCA8) for active input states or to distinguish clickable card groups.

## Shapes

The shape language is consistently **Rounded**, reinforcing the friendly and approachable brand personality.

- **Base Radius:** `0.5rem` (8px) for standard UI elements like buttons and input fields.
- **Large Radius:** Use `1rem` (16px) for primary containers and financial summary cards to make them feel distinct and "softer" than smaller utility components.
- **Interactive Elements:** Buttons should always use `rounded-lg` (1rem) or even pill-shapes for CTAs to maximize their perceived clickability and tactile nature.

## Components

### Buttons
- **Primary:** Background `primary` (#0F3D3E), text White. Semi-bold text. Use for "Entrar con Google" or "Guardar".
- **Secondary:** Background `soft-card`, border `border-soft`, text `neutral`. Use for "Ver ejemplo" or "Cancelar".
- **Success:** Background `secondary` (#4F8A5B), text White. Use sparingly for final positive actions like "Confirmar Ingreso".

### Cards (SummaryCard)
- The "Te queda para el mes" card is the most important component. It should use `stat-lg` for the value and a larger `1.5rem` corner radius. 
- Include a descriptive label below the value in `text-secondary` (e.g., "Considerando gastos pendientes").

### Transaction Items
- Horizontal layout. Left: Icon inside a `rounded-md` square with a semantic background (e.g., `mint-bg` for income). Center: Bold description with category/date subtext. Right: Bold financial amount with `+/-` prefix.
- Colors: Positive values in `secondary` green; negative in `neutral` navy or `muted-coral` if it's an alert-worthy expense.

### Inputs & Forms
- Background `soft-card`, `1px` border `border-soft`.
- Focus state: Border transitions to `secondary` green with a subtle `2px` focus ring in `mint-bg`.

### Empty States
- Dotted border `border-soft`, centered icon, and helpful Spanish copy with a primary CTA to "Agregar movimiento".