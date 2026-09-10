import { Link } from 'react-router-dom'
import useSeo from './useSeo'

export default function AboutPage() {
  useSeo('About Us', 'History, mission, vision and leadership of ABC College of Higher Education.')
  return (
    <div className="page">
      <h1>About ABC College</h1>
      <p>
        Founded in 1985, ABC College of Higher Education has grown from a single science block
        into a 40-acre residential campus serving more than 5,000 students. We are accredited
        NAAC A+ and ranked among the region's top ten institutions for graduate outcomes.
      </p>
      <div className="card-grid">
        <article className="card"><h3>Our Mission</h3><p className="muted">To make high-quality, career-focused education accessible to every meritorious student, regardless of background.</p></article>
        <article className="card"><h3>Our Vision</h3><p className="muted">To be the first-choice college in the region for students, recruiters and research partners by 2030.</p></article>
        <article className="card"><h3>Our Values</h3><p className="muted">Academic integrity, curiosity, inclusion and service to the community.</p></article>
      </div>
      <h2>Leadership</h2>
      <p>
        The college is led by Principal <strong>Dr. Anita Rao</strong> (Ph.D., IIT Bombay) with a
        governing council of academics and industry leaders. Each of our twenty departments is
        headed by an experienced professor-administrator.
      </p>
      <Link to="/contact" className="btn btn-primary">Get in touch</Link>
    </div>
  )
}
