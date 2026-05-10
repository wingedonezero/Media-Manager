# PB Dark Template TV Shows

A dark-theme tinyMediaManager template for TV shows, inspired by Emby.

## Features

- TV show list with fuzzy title search (accent- and case-insensitive)
- Responsive dark UI with fanart banner, poster, and show info
- Detail page: title, year, season/episode counts, rating, genres, studio
- Detail page: collapsible cast section (photo, name, role; closed by default)
- Detail page: episodes grouped by season in collapsible sections (open by default)
- Lazy image loading via `IntersectionObserver`
- Full internationalisation: EN, FR, ES, IT, RU, ZH, VI
  - Browser language auto-detection with fallback to English
  - Language choice persisted in `localStorage`
- Exportable to the same directory as PB Dark Template Movies without
  filename conflict

## Origin

Forked from Dark Template (original source: https://buron.coffee/files/darkTemplate,
link no longer active). See the root `README.md` for full acknowledgements.
