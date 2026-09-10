import { useEffect, useMemo, useState } from 'react'
import { listStudents, listDepartments } from '../../services/peopleService'
import DataTable from '../../components/DataTable'
import { Loading, Empty, ErrorState } from '../../components/States'

/** Faculty view: browse the student roster with department filter. */
export default function FacultyStudentsPage() {
  const [rows, setRows] = useState(null)
  const [departments, setDepartments] = useState([])
  const [departmentId, setDepartmentId] = useState('')
  const [search, setSearch] = useState('')
  const [error, setError] = useState(false)

  useEffect(() => {
    listDepartments().then(({ data }) => setDepartments(data)).catch(() => {})
  }, [])

  useEffect(() => {
    let active = true
    setError(false)
    const params = { size: 200 }
    if (departmentId) params.departmentId = departmentId
    if (search) params.search = search
    listStudents(params)
      .then(({ data }) => active && setRows(data.content || []))
      .catch(() => active && setError(true))
    return () => { active = false }
  }, [departmentId, search])

  const filtered = useMemo(() => rows || [], [rows])

  return (
    <div>
      <h1>Students</h1>
      <p className="muted small">Class roster across all departments — search by name, roll number or email.</p>

      <div className="filter-row" style={{ display: 'flex', gap: '.8rem', margin: '1rem 0', flexWrap: 'wrap' }}>
        <input
          placeholder="Search students…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search students"
          style={{ flex: '1 1 220px', maxWidth: 320 }}
        />
        <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)} aria-label="Filter by department">
          <option value="">All departments</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
        </select>
      </div>

      {error && <ErrorState onRetry={() => setRows(null)} />}
      {rows === null && !error && <Loading />}
      {rows !== null && filtered.length === 0 && !error && <Empty message="No students match these filters." />}
      {filtered.length > 0 && (
        <DataTable
          columns={['Roll No', 'Name', 'Course', 'Department', 'Semester', 'Status']}
          rows={filtered.map((s) => [
            s.studentId,
            s.fullName,
            s.courseName,
            s.departmentName,
            s.semesterName,
            s.status,
          ])}
        />
      )}
    </div>
  )
}
