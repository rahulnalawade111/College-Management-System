import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import publicService from '../../services/publicService'

const FACILITIES = [
  { icon: '📚', name: 'Central Library', desc: '120,000+ volumes, digital journals and 24×7 reading halls.' },
  { icon: '🔬', name: 'Laboratories', desc: 'Industry-grade labs across every engineering and science department.' },
  { icon: '🏠', name: 'Hostels', desc: 'Separate residential facilities for 2,000+ students with mess and recreation.' },
  { icon: '⚽', name: 'Sports Complex', desc: 'Olympic-size pool, indoor courts, athletics track and fitness centre.' },
  { icon: '🍽️', name: 'Cafeteria', desc: 'Multi-cuisine food court and cafés across the campus.' },
  { icon: '💻', name: 'Computer Center', desc: 'High-performance computing labs with 1 Gbps campus-wide internet.' },
  { icon: '💼', name: 'Placement Cell', desc: 'Dedicated training and a 95% placement record with 150+ recruiters.' },
  { icon: '🚌', name: 'Transport', desc: 'College buses on 40+ routes connecting the city and suburbs.' },
]

const GALLERY = [
  { src: '/assets/gallery/campus.svg', alt: 'Main campus building with green lawns' },
  { src: '/assets/gallery/library.svg', alt: 'Students studying in the central library' },
  { src: '/assets/gallery/lab.svg', alt: 'Students working in an engineering laboratory' },
  { src: '/assets/gallery/sports.svg', alt: 'Inter-college sports meet on the athletics track' },
  { src: '/assets/gallery/graduation.svg', alt: 'Graduation day ceremony at the central lawn' },
  { src: '/assets/gallery/fest.svg', alt: 'Students performing at the annual cultural fest' },
  { src: '/assets/gallery/hostel.svg', alt: 'Student hostel residential block' },
  { src: '/assets/gallery/cafeteria.svg', alt: 'Campus cafeteria food court' },
]

export default function HomePage() {
  const [notices, setNotices] = useState(null)
  const [events, setEvents] = useState(null)

  useEffect(() => {
    document.title = 'ABC College of Higher Education — Empowering Students. Building Futures.'
    const meta = document.querySelector('meta[name="description"]')
    if (meta) meta.setAttribute('content',
      'ABC College of Higher Education — 20+ departments, 50+ programs, a 95% placement record and a modern residential campus. Admissions open for 2026–27.')

    let active = true
    publicService.notices().then((d) => active && setNotices(d.slice(0, 4))).catch(() => active && setNotices([]))
    publicService.events().then((d) => active && setEvents(d.slice(0, 4))).catch(() => active && setEvents([]))
    return () => { active = false }
  }, [])

  return (
    <div className="public-home">
      <section className="hero">
        <div className="hero-inner">
          <p className="hero-kicker">Admissions open · 2026–2027</p>
          <h1>Empowering Students. Building Futures.</h1>
          <p className="hero-sub">
            ABC College of Higher Education — a modern campus with 20+ departments, 50+ programs,
            and a 95% placement record.
          </p>
          <div className="hero-actions">
            <Link to="/admissions" className="btn btn-accent btn-lg">Apply Now</Link>
            <Link to="/courses" className="btn btn-outline btn-lg">Explore Courses</Link>
          </div>
        </div>
      </section>

      <section className="stats-band" aria-label="College statistics">
        <div className="stats-inner">
          <div className="stat"><strong>5000+</strong><span>Students</span></div>
          <div className="stat"><strong>200+</strong><span>Faculty</span></div>
          <div className="stat"><strong>20+</strong><span>Departments</span></div>
          <div className="stat"><strong>50+</strong><span>Programs</span></div>
          <div className="stat"><strong>95%</strong><span>Placement</span></div>
        </div>
      </section>

      <section className="section">
        <h2>About the College</h2>
        <p>
          For four decades, ABC College of Higher Education has shaped graduates who lead in
          engineering, science, commerce and the arts. Our teaching combines rigorous academics
          with hands-on projects, industry internships and a vibrant campus life — so students
          leave not just with a degree, but with the confidence to build their futures.
        </p>
        <Link to="/about" className="btn btn-primary">More about us</Link>
      </section>

      <section className="section section-alt">
        <h2>Featured Programs</h2>
        <div className="card-grid">
          {[
            { t: 'B.Tech — Computer Science', d: 'AI, systems and software engineering with a project-led curriculum.' },
            { t: 'BBA', d: 'Management fundamentals, live case studies and a summer internship.' },
            { t: 'B.Sc. Data Science', d: 'Statistics, machine learning and applied analytics from year one.' },
          ].map((c) => (
            <article key={c.t} className="card">
              <h3>{c.t}</h3>
              <p className="muted">{c.d}</p>
              <Link to="/courses" className="text-link">View programs →</Link>
            </article>
          ))}
        </div>
      </section>

      <section className="section">
        <h2>Our Departments</h2>
        <p className="muted">Twenty departments spanning engineering, sciences, commerce and humanities.</p>
        <Link to="/departments" className="btn btn-primary">Browse departments</Link>
      </section>

      <section className="section section-alt principal">
        <img src="/assets/principal.svg" alt="Portrait of the Principal, Dr. Anita Rao"
             className="principal-photo" loading="lazy" width="160" height="160" />
        <blockquote>
          <p>
            “Our promise is simple: every student who walks through our gates leaves prepared —
            technically, professionally and personally — for the world ahead.”
          </p>
          <footer className="muted">Dr. Anita Rao — Principal</footer>
        </blockquote>
      </section>

      <section className="section" aria-label="Latest notices">
        <div className="section-head-row">
          <h2>Latest Notices</h2>
          <Link to="/notices" className="text-link">All notices →</Link>
        </div>
        {notices === null && <p className="muted">Loading notices…</p>}
        {notices !== null && notices.length === 0 && <p className="muted">No notices published yet — check back soon.</p>}
        <ul className="notice-list">
          {(notices || []).map((n) => (
            <li key={n.id} className="notice-item">
              <strong>{n.title}</strong>
              {n.body && <p className="muted small clamp-2">{n.body}</p>}
              {n.publishedAt && <time className="muted small">{new Date(n.publishedAt).toLocaleDateString()}</time>}
            </li>
          ))}
        </ul>
      </section>

      <section className="section section-alt" aria-label="Upcoming events">
        <div className="section-head-row">
          <h2>Upcoming Events</h2>
          <Link to="/events" className="text-link">All events →</Link>
        </div>
        {events === null && <p className="muted">Loading events…</p>}
        {events !== null && events.length === 0 && <p className="muted">No upcoming events — see the full calendar for past highlights.</p>}
        <div className="event-grid">
          {(events || []).map((e) => (
            <article key={e.id} className="event-card">
              <div className="event-date" aria-hidden="true">
                <span className="event-day">{new Date(e.eventDate).getDate()}</span>
                <span className="event-month">{new Date(e.eventDate).toLocaleString('en', { month: 'short' })}</span>
              </div>
              <div>
                <h3>{e.title}</h3>
                {e.venue && <p className="muted small">📍 {e.venue}</p>}
                {e.description && <p className="muted small clamp-2">{e.description}</p>}
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="section" aria-label="Campus facilities">
        <h2>Campus Facilities</h2>
        <div className="facility-grid">
          {FACILITIES.map((f) => (
            <div key={f.name} className="facility">
              <span className="facility-icon" aria-hidden="true">{f.icon}</span>
              <h3>{f.name}</h3>
              <p className="muted small">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="section section-alt" aria-label="Campus gallery">
        <h2>Campus Life</h2>
        <div className="gallery-grid">
          {GALLERY.map((g) => (
            <img key={g.src} src={g.src} alt={g.alt} loading="lazy" width="320" height="200"
                 onError={(ev) => { ev.currentTarget.src = '/assets/gallery/fallback.svg' }} />
          ))}
        </div>
      </section>
    </div>
  )
}
