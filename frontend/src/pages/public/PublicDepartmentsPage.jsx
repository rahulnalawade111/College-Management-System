import { useEffect, useState } from 'react'
import useSeo from './useSeo'
import publicService from '../../services/publicService'

const HOD_FALLBACK = 'Head of Department'

export default function DepartmentsPage() {
  useSeo('Departments', 'The twenty academic departments of ABC College of Higher Education and their programs.')
  const [departments, setDepartments] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let active = true
    publicService.departments()
      .then((d) => active && setDepartments(d))
      .catch(() => active && setError(true))
    return () => { active = false }
  }, [])

  return (
    <div className="page">
      <h1>Departments</h1>
      <p className="muted">Twenty departments spanning engineering, sciences, commerce and humanities.</p>
      {error && <p className="alert alert-danger">Could not load departments right now — please try again shortly.</p>}
      {departments === null && !error && <p className="muted">Loading departments…</p>}
      <div className="card-grid">
        {(departments || []).map((d) => (
          <article key={d.id} className="card">
            <h3>{d.departmentName}</h3>
            <p className="muted small">Code: {d.departmentCode}</p>
            {d.description && <p>{d.description}</p>}
            {d.hodName && <p className="small"><strong>HOD:</strong> {d.hodName}</p>}
          </article>
        ))}
      </div>
      {departments !== null && departments.length === 0 && !error && (
        <p className="muted">Department listings will be published soon.</p>
      )}
    </div>
  )
}
