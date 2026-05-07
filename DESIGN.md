# Project Purr — Design System

## Phase 2A — Premium Ritual Direction

Phase 1C validated the sensory illusion. Phase 2A is about emotional product quality, not feature growth.

### Intent

Project Purr should feel like a warm, trusted nighttime companion. The interface supports the ritual, then fades away.

### What changes in Phase 2A

- Prioritize atmosphere over information density.
- Prioritize emotional comfort over feature visibility.
- Prioritize soft continuity over energetic interactions.
- Keep controls available but visually secondary during active sessions.

### Emotional qualities to preserve

- Deep dark atmosphere with gentle warm highlights.
- Low visual noise and restrained chrome.
- Calm typography and generous spacing.
- Slow, ambient motion (fades, breathing glows, soft transitions).

### Explicit anti-patterns (still prohibited)

- Sleep dashboard language, quantified-self framing, and data-heavy surfaces.
- Bright/neon accents, hard contrast spikes, and flashy animation.
- Feature expansion outside scope (accounts, subscriptions, social, analytics, multi-profile systems).

---

## On the color question

The reference app's dark navy + electric blue palette is excellent for a *meditation/sleep tech* product. But Project Purr is not that. It is tactile, warm, intimate — *a cat on your chest at 2am*. Blue reads as cool, clinical, digital. It would make Purr look like every other wellness app on the store.

**Recommendation: keep warm, go deeper.**

Borrow the reference's *design patterns* (glassmorphism cards, full-bleed dark backgrounds, large editorial typography, floating play buttons) but keep the color identity warm. The existing palette is directionally right — it just needs more saturation and contrast to carry the new screen complexity.

---

## Color Palette

### Base
| Token | Hex | Usage |
|---|---|---|
| `Background` | `#0F0B08` | App background (darker than current #1B1612 — more depth) |
| `Surface` | `#1A1410` | Card bases, bottom sheets |
| `SurfaceVariant` | `#241C15` | Elevated surfaces, input fields |

### Accent
| Token | Hex | Usage |
|---|---|---|
| `Primary` | `#E09040` | Play button, selected state, key highlights |
| `PrimaryDim` | `#A06428` | Secondary actions, pressed states |
| `PrimaryGlow` | `#E0904020` | Subtle glow behind active play button |

> **Why this amber over the current #D4A574?**
> The current tan is too desaturated — it reads as beige against dark brown. #E09040 is richer, has genuine warmth (cat's eye, candlelight), and pops clearly against the dark background without being harsh.

### Text
| Token | Hex | Usage |
|---|---|---|
| `OnBackground` | `#F5EAD8` | Primary text (warm cream, not pure white) |
| `OnSurface` | `#F0E0C8` | Text on cards |
| `TextSecondary` | `#9C8068` | Labels, metadata, inactive states |
| `TextTertiary` | `#6A5040` | Placeholder, very dim states |

### Glass surfaces (Glassmorphism)
Compose implementation: `background(Brush)` with `BlurMaskFilter` or via `graphicsLayer`.

| Token | Value |
|---|---|
| `GlassFill` | `Color(0xFFFFEED8).copy(alpha = 0.07f)` |
| `GlassBorder` | `Color(0xFFFFEED8).copy(alpha = 0.12f)` |
| `GlassBorderWidth` | `0.5.dp` |
| `GlassCorner` | `20.dp` |

---

## Typography

Switch from `FontFamily.SansSerif` (system default) to **Inter** or **DM Sans** from Google Fonts — these are the same family class as the reference but free.

For the large display headlines (onboarding), add a **display style** at 52sp, light weight, wide letter spacing — creates the editorial "Stop and take a deep breath" feel.

```
DisplayLarge   52sp  Light (300)   -0.5sp   onboarding hero text
HeadlineLarge  32sp  SemiBold      0.0sp    section titles, cat name on Home
HeadlineMedium 24sp  Medium        0.1sp    screen titles (keep existing)
TitleLarge     20sp  SemiBold      0.0sp    card titles
BodyLarge      16sp  Regular       0.15sp   body copy (keep existing)
BodyMedium     14sp  Regular       0.1sp    secondary body
LabelLarge     14sp  Medium        0.1sp    button labels, control labels
LabelSmall     11sp  Medium        0.5sp    chips, tags, metadata
```

---

## Spacing & Layout

8dp grid system throughout.

| Token | Value | Usage |
|---|---|---|
| `SpaceXS` | `4.dp` | Icon internal padding |
| `SpaceS` | `8.dp` | Between related elements |
| `SpaceM` | `16.dp` | Card internal padding, between sections |
| `SpaceL` | `24.dp` | Screen horizontal padding |
| `SpaceXL` | `32.dp` | Between major sections |
| `SpaceXXL` | `48.dp` | Onboarding hero spacing |

---

## Components

### Glass Card
Used for: cat profile cards on Home, controls panel on Session screen.
```
Background:   GlassFill + GlassBorder stroke
Corner:       20.dp
Padding:      20.dp
Elevation:    none (glassmorphism replaces shadow)
```

### Play Button (Primary)
```
Shape:        Circle
Size:         88.dp (Home card), 120.dp (Session screen)
Color:        Primary (#E09040)
Icon:         PlayArrow / Pause, white
Active glow:  4.dp amber radial blur behind button when session active
```

### Sleep Timer Chips
```
Shape:        RoundedCorner 50% (pill)
Selected:     Primary fill, dark text
Unselected:   GlassFill, TextSecondary text
Height:       36.dp
```

### Toggle Row
```
Layout:       SpaceBetween Row, verticalCenter
Label:        LabelLarge, OnSurface
Switch:       Material3 Switch, thumbColor=Primary, trackColor=PrimaryDim
Divider:      none (spacing only)
```

---

## Screen Layouts

### 1. Onboarding (3 slides)

```
[Full-bleed dark photo background with warm overlay gradient]
  ↕ flex
[Large display text — left-aligned]
  "Feel it."            slide 1
  "Not just sound."     slide 2
  "Your companion,
   anytime."            slide 3
  ↕ 16dp
[Body text — 2 lines max, secondary color]
  ↕ flex
[Bottom row]
  [Progress dots]       [Next →  or  Get Started]
```

Photos: dark, atmospheric — close cat fur, cat eye in dim light, hand + cat.  
No cartoon, no illustration. Same energy as the reference's underwater/nebula imagery.

### 2. Home Screen

```
[Status bar]
[AppBar: "Purr"  logo left, no nav right]
  ↕ 32dp
[Section: "Your companion"]
  ↕ 8dp
[Featured Card — House Cat]
  [Glass card, full width, ~180dp tall]
  [Left: Cat name "House Cat" H1, tagline body]
  [Right: 88dp play button circle]
  [Bottom: duration chip (∞), mood tags]
  ↕ 24dp
[Section: "More companions"  +  "Coming soon"]
  ↕ 8dp
[Horizontal scroll row: Ragdoll card, Maine Coon card — both locked/dim]
```

### 3. Session Screen

```
[Full-bleed warm dark background]
  ↕ 24dp
[Top row: ← back icon   ...  (three-dot: timer info)]
  ↕ flex
[Center: cat name HeadlineLarge]
        [phase subtitle BodyMedium, secondary]
        ↕ 32dp
        [120dp play/pause circle, amber glow when active]
  ↕ flex
[Controls glass card — bottom panel]
  [Silent Purr toggle row]
  [Chest Mode toggle row]
  [Sleep timer chips row]
```

When **Chest Mode active + playing**: entire UI alpha dims to 0.06, system bars hidden.  
Single tap anywhere on screen: briefly restore alpha to 1.0 for 3 seconds then re-dim.

### 4. Future: Browse / Profile screen
Not in this phase. Placeholder "Coming soon" cards on Home are sufficient.

---

## Motion Principles

- **No jarring transitions** — slide-in (300ms, ease-out) for screen navigation
- **Play button press**: scale 0.92 on press, spring back — tactile acknowledgment
- **Session active glow**: slow ambient pulse on play button (3s period), very subtle (opacity 0.3→0.6)
- **Chest mode transition**: alpha fade to dim over 800ms — not instant
- **Onboarding slides**: horizontal swipe or tap-to-advance, no auto-advance

---

## What NOT to do

- No pure white text (use warm cream #F5EAD8)
- No blue anywhere — no tints, no states, no links
- No cartoon cat illustrations in Phase 1
- No bottom navigation bar (one core flow, no multi-tab)
- No rating nag, no upgrade prompts, no notification opt-in — ever during a session
- No light mode
