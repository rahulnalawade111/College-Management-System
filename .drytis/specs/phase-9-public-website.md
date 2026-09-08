# Phase 9 — Public college website, SEO, performance, deployment, GitHub

## Goal
Professional public-facing website (fed by live notices/events), SEO/performance pass,
production build, complete README, and the final push to GitHub.

## Files
- PublicLayout (header: logo, college name, full nav incl. mobile hamburger; footer:
  address/email/phone, social, quick links, admissions, student portal).
- Pages (React Router, public): /, /about, /academics, /departments, /courses, /admissions,
  /faculty, /events, /notices, /gallery, /contact.
- Homepage sections: hero ("ABC College of Higher Education" / "Empowering Students.
  Building Futures." / Apply Now + Explore Courses), statistics band (5000+/200+/20+/50+/95%),
  about, course cards, department cards, principal message (photo+text), latest notices
  (live API), upcoming events (live API), facilities grid (library, laboratories, hostel,
  sports, cafeteria, computer center, placement cell), responsive gallery, footer.
- Contact form → backend endpoint storing messages (simple admin view) + MailService mock.
- SEO: react-helmet-async per-page titles/descriptions, semantic HTML, sitemap.xml +
  robots.txt, Open Graph tags, accessible navigation (aria, focus states, alt text).
- Performance: production Vite build, code-split routes, lazy images (gallery),
  memoized dashboard charts; lighthouse-style sanity pass.
- Production config: `npm run build` output served by Caddy (or vite preview service);
  backend run from jar (`mvn clean package`) as background service; application-prod
  properties documented (DB, JWT_SECRET, CORS allowlist, file dir).
- README.md: requirements, prerequisites, MySQL setup, backend setup, frontend setup,
  environment variables table, Flyway/db setup, run instructions, demo credentials
  (marked DEV ONLY, change before production), API docs pointer (Swagger), production
  deployment instructions, GitHub push instructions.
- GitHub: initialize repo history, create `.github/` workflows only if desired (optional),
  push final code to the user-provided GitHub repository (URL + token to be requested).

## Acceptance criteria
- [ ] The public homepage renders hero, stats, about, courses, departments, principal
      message, live notices, upcoming events, facilities, gallery and footer.
- [ ] Notices and events on the public pages come from the backend API (visible in network
      tab) and update when an admin publishes a new one.
- [ ] All listed public pages navigate without errors; nav collapses to a hamburger menu
      on mobile.
- [ ] Google Lighthouse-style checks pass: per-page titles/descriptions present, images
      have alt text, interactive elements keyboard-accessible.
- [ ] The production build of the frontend is served (not the dev server) and the app
      still works end-to-end (login, a CRUD flow, a report download).
- [ ] README covers all 10 required sections and lists the demo credentials with a clear
      "change before production" warning.
- [ ] The complete codebase is pushed to GitHub and the URL is reported to the user.

## Tests
Smoke test: build passes, public routes render, notices/events API consumers handle empty
and error states; contact form validation.

## Edge cases
Deep-linking to public pages (client routing fallback), slow API on homepage (sections
degrade gracefully), image fallbacks when gallery/event images missing.
