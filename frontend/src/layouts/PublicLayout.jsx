import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'

const NAV = [
  { to: '/', label: 'Home' },
  { to: '/about', label: 'About' },
  { to: '/academics', label: 'Academics' },
  { to: '/departments', label: 'Departments' },
  { to: '/courses', label: 'Courses' },
  { to: '/admissions', label: 'Admissions' },
  { to: '/faculty', label: 'Faculty' },
  { to: '/events', label: 'Events' },
  { to: '/notices', label: 'Notices' },
  { to: '/gallery', label: 'Gallery' },
  { to: '/contact', label: 'Contact' },
]

export default function PublicLayout() {
  const [open, setOpen] = useState(false)

  useEffect(() => {
    setOpen(false)
    window.scrollTo(0, 0)
  }, [])

  return (
    <div className="public-shell">
      <a href="#main" className="skip-link">Skip to main content</a>
      <header className="public-header">
        <div className="public-header-inner">
          <Link to="/" className="brand" aria-label="ABC College of Higher Education — home">
            <span className="brand-badge" aria-hidden="true">AC</span>
            <span className="brand-text">
              <strong>ABC College</strong>
              <small>of Higher Education</small>
            </span>
          </Link>
          <button
            className="nav-toggle"
            aria-label="Toggle navigation menu"
            aria-expanded={open}
            onClick={() => setOpen((v) => !v)}
          >
            {open ? '✕' : '☰'} Menu
          </button>
          <nav className={`public-nav ${open ? 'open' : ''}`} aria-label="Main navigation">
            {NAV.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) => (isActive ? 'active' : '')}
              >
                {item.label}
              </NavLink>
            ))}
            <Link to="/login" className="btn btn-accent nav-cta">Student Portal</Link>
          </nav>
        </div>
      </header>

      <main id="main">
        <Outlet />
      </main>

      <footer className="public-footer">
        <div className="footer-grid">
          <div>
            <h3>ABC College of Higher Education</h3>
            <p>Empowering Students. Building Futures.</p>
            <p className="muted">NAAC A+ accredited · Established 1985</p>
          </div>
          <div>
            <h4>Contact</h4>
            <p>123 University Road, Knowledge City<br />Phone: +91 12345 67890<br />Email: info@abccollege.edu</p>
          </div>
          <div>
            <h4>Quick Links</h4>
            <ul>
              <li><Link to="/admissions">Admissions</Link></li>
              <li><Link to="/notices">Notices</Link></li>
              <li><Link to="/events">Events</Link></li>
              <li><Link to="/gallery">Gallery</Link></li>
            </ul>
          </div>
          <div>
            <h4>Portals</h4>
            <ul>
              <li><Link to="/login">Student / Faculty / Parent login</Link></li>
              <li><Link to="/admissions">Apply Now</Link></li>
              <li><Link to="/contact">Contact Us</Link></li>
            </ul>
          </div>
        </div>
        <div className="footer-bottom">
          © {new Date().getFullYear()} ABC College of Higher Education. All rights reserved.
        </div>
      </footer>
    </div>
  )
}
