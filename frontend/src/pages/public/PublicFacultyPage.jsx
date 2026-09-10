import useSeo from './useSeo'

const FACULTY_HIGHLIGHTS = [
  { name: 'Dr. Anita Rao', role: 'Principal · Ph.D. IIT Bombay', area: 'Computer Science' },
  { name: 'Dr. Suresh Menon', role: 'Dean, Sciences', area: 'Physics' },
  { name: 'Prof. Kavya Iyer', role: 'Head, Management Studies', area: 'Marketing & Strategy' },
  { name: 'Dr. Rohit Sharma', role: 'Head, Data Science', area: 'Machine Learning' },
]

export default function FacultyPage() {
  useSeo('Faculty', 'Meet the 200+ faculty members of ABC College of Higher Education.')
  return (
    <div className="page">
      <h1>Our Faculty</h1>
      <p>
        200+ full-time faculty members — 40% with doctorates from institutions of national
        importance — combine research active careers with a passion for teaching.
      </p>
      <div className="card-grid">
        {FACULTY_HIGHLIGHTS.map((f) => (
          <article key={f.name} className="card faculty-card">
            <img src="/assets/faculty-placeholder.svg" alt={`Portrait of ${f.name}`}
                 className="faculty-photo" loading="lazy" width="96" height="96" />
            <h3>{f.name}</h3>
            <p className="muted small">{f.role}</p>
            <p className="small">Area: {f.area}</p>
          </article>
        ))}
      </div>
    </div>
  )
}
