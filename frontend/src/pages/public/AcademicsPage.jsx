import { Link } from 'react-router-dom'
import useSeo from './useSeo'

export default function AcademicsPage() {
  useSeo('Academics', 'Academic structure at ABC College: semesters, credit system, examinations and academic calendar.')
  return (
    <div className="page">
      <h1>Academics</h1>
      <p>
        Programs follow a choice-based credit system with two semesters per academic year.
        Continuous assessment (assignments, attendance and mid-terms) combines with
        end-semester examinations; results, SGPA and CGPA are published on the student portal.
      </p>
      <div className="card-grid">
        <article className="card"><h3>Semester System</h3><p className="muted">Odd semester: June–November. Even semester: December–April. A dedicated summer term supports backlog clearance.</p></article>
        <article className="card"><h3>Examinations</h3><p className="muted">Two internal assessments plus a 70-mark end-semester exam per course. Timetables are published on the portal.</p></article>
        <article className="card"><h3>Grading</h3><p className="muted">10-point scale from O (outstanding, ≥90%) to F (fail). SGPA per semester and cumulative CGPA are computed automatically.</p></article>
      </div>
      <h2>Academic Calendar Highlights</h2>
      <ul>
        <li>Orientation for new students — first week of June</li>
        <li>Mid-semester examinations — weeks 8–9 of each term</li>
        <li>Annual convocation — December</li>
      </ul>
      <Link to="/courses" className="btn btn-primary">Explore programs</Link>
    </div>
  )
}
