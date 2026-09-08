import { useEffect, useState, useCallback } from 'react'
import { listSubjects, listStudents } from '../../services/peopleService'
import {
  bulkMarkAttendance, getAttendanceForSubjectDate,
} from '../../services/attendanceService'
import { pickErrorMessage } from '../../components/States'
import { useToast } from '../../context/ToastContext'

const STATUSES = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED']

/** Color-coded status pill used in marking and read-only views. */
export function StatusPill({ status }) {
  const cls = { PRESENT: 'att-present', ABSENT: 'att-absent', LATE: 'att-late', EXCUSED: 'att-excused' }[status] || ''
  return <span className={`att-pill ${cls}`}>{status}</span>
}

export default function MarkAttendancePage() {
  const { success, error } = useToast()
  const [subjects, setSubjects] = useState([])
  const [subjectId, setSubjectId] = useState('')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [students, setStudents] = useState([])
  const [marks, setMarks] = useState({})
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [alreadyMarked, setAlreadyMarked] = useState(false)

  useEffect(() => {
    listSubjects({ size: 200 }).then(({ data }) => {
      const list = data.content || data
      setSubjects(list)
      if (list.length) setSubjectId(list[0].id.toString())
    }).catch(() => {})
  }, [])

  const loadRoster = useCallback(async () => {
    if (!subjectId) return
    const subject = subjects.find((s) => s.id === Number(subjectId))
    setLoading(true)
    setAlreadyMarked(false)
    try {
      const params = subject?.semesterId ? { semesterId: subject.semesterId, size: 100, status: 'ACTIVE' } : { size: 100, status: 'ACTIVE' }
      const { data } = await listStudents(params)
      const roster = data.content || []
      setStudents(roster)
      const initial = {}
      roster.forEach((s) => { initial[s.id] = 'PRESENT' })
      // pre-fill saved values for this subject+date
      try {
        const saved = await getAttendanceForSubjectDate(subjectId, date)
        if (saved.data.length) {
          setAlreadyMarked(true)
          saved.data.forEach((row) => { initial[row.studentId] = row.status })
        }
      } catch { /* none saved yet */ }
      setMarks(initial)
    } catch (e) {
      error(pickErrorMessage(e, 'Failed to load class roster'))
    } finally {
      setLoading(false)
    }
  }, [subjectId, date, subjects, error])

  useEffect(() => { loadRoster() }, [loadRoster])

  const markAllPresent = () => {
    const next = {}
    students.forEach((s) => { next[s.id] = 'PRESENT' })
    setMarks(next)
  }

  const save = async () => {
    if (!students.length) return
    setSaving(true)
    try {
      const entries = students.map((s) => ({ studentId: s.id, status: marks[s.id] || 'PRESENT' }))
      await bulkMarkAttendance({ subjectId: Number(subjectId), attendanceDate: date, entries })
      success(`Attendance saved for ${entries.length} students`)
      setAlreadyMarked(true)
    } catch (e) {
      error(pickErrorMessage(e, 'Save failed'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Mark Attendance</h1>
          <p className="muted">Select subject and date, then mark each student</p>
        </div>
      </div>

      <div className="filters">
        <select value={subjectId} onChange={(e) => setSubjectId(e.target.value)}>
          {subjects.map((s) => (
            <option key={s.id} value={s.id}>
              {s.subjectCode} — {s.subjectName} (Sem {s.semesterName?.replace('Semester ', '') || '?'})
            </option>
          ))}
        </select>
        <input type="date" value={date} max={new Date().toISOString().slice(0, 10)}
          onChange={(e) => setDate(e.target.value)} />
        <button type="button" className="btn btn-outline-dark" onClick={markAllPresent}>
          ✓ Mark All Present
        </button>
        <button type="button" className="btn" onClick={save} disabled={saving || !students.length || alreadyMarked}>
          {alreadyMarked ? 'Already saved' : saving ? 'Saving…' : 'Save attendance'}
        </button>
      </div>

      {alreadyMarked && (
        <div className="att-notice">
          Attendance for this subject and date was already saved — values below are read-only.
          Pick a different date to mark again.
        </div>
      )}

      {loading ? <div className="state-box"><span className="spinner" /> Loading roster…</div> :
        students.length === 0 ? (
          <div className="state-box">📭 No active students found for this semester.</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Student ID</th><th>Name</th><th>Status</th><th>Mark</th>
                </tr>
              </thead>
              <tbody>
                {students.map((s) => (
                  <tr key={s.id}>
                    <td>{s.studentId}</td>
                    <td>{s.fullName}</td>
                    <td><StatusPill status={marks[s.id]} /></td>
                    <td>
                      <div className="att-options">
                        {STATUSES.map((st) => (
                          <button
                            key={st} type="button" disabled={alreadyMarked}
                            className={`att-opt ${marks[s.id] === st ? `sel-${st.toLowerCase()}` : ''}`}
                            onClick={() => setMarks((m) => ({ ...m, [s.id]: st }))}
                          >
                            {st[0] + st.slice(1).toLowerCase()}
                          </button>
                        ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
    </div>
  )
}
