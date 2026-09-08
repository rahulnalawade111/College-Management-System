import { Link } from 'react-router-dom'

export default function HomePage() {
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
            <Link to="/login" className="btn btn-accent btn-lg">Apply Now</Link>
            <Link to="/login" className="btn btn-outline btn-lg">Explore Courses</Link>
          </div>
        </div>
      </section>

      <section className="stats-band">
        <div className="stats-inner">
          <div className="stat"><strong>5000+</strong><span>Students</span></div>
          <div className="stat"><strong>200+</strong><span>Faculty</span></div>
          <div className="stat"><strong>20+</strong><span>Departments</span></div>
          <div className="stat"><strong>50+</strong><span>Programs</span></div>
          <div className="stat"><strong>95%</strong><span>Placement</span></div>
        </div>
      </section>

      <section className="section">
        <h2>Portal sign-in</h2>
        <p className="muted">Students, faculty, parents and administrators — sign in to your dashboard.</p>
        <Link to="/login" className="btn btn-primary">Go to login</Link>
      </section>
    </div>
  )
}
