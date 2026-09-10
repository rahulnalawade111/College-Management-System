import { Link } from 'react-router-dom'
import useSeo from './useSeo'

const PROGRAMS = [
  { level: 'Undergraduate', items: ['B.Tech (8 specialisations)', 'B.Sc. (Physics, Chemistry, Maths, Data Science)', 'BBA', 'B.Com (Hons)', 'BA (Economics, Psychology, English)'] },
  { level: 'Postgraduate', items: ['M.Tech (6 specialisations)', 'MBA', 'M.Sc. (Maths, Data Science)', 'M.Com'] },
  { level: 'Doctoral', items: ['Ph.D. across all engineering and science departments'] },
]

export default function CoursesPage() {
  useSeo('Courses & Programs', 'Undergraduate, postgraduate and doctoral programs offered at ABC College of Higher Education.')
  return (
    <div className="page">
      <h1>Courses & Programs</h1>
      <p className="muted">50+ programs across three levels, all on a choice-based credit system.</p>
      {PROGRAMS.map((p) => (
        <section key={p.level} style={{ marginBottom: '1.6rem' }}>
          <h2>{p.level}</h2>
          <ul>{p.items.map((i) => <li key={i}>{i}</li>)}</ul>
        </section>
      ))}
      <Link to="/admissions" className="btn btn-accent btn-lg">Apply Now</Link>
    </div>
  )
}
