import { useEffect, useState } from 'react'
import { getMyAttendanceSummary, getMyAttendance, getStudentAttendanceSummary, getStudentAttendance } from '../../services/attendanceService'
import { pickErrorMessage } from '../../components/States'

/** Color band: ≥80 green, 60–79 amber, <60 red. */
function pctClass(pct) {
  if (pct >= 80) return 'pct-good'
  if (pct >= 60) return 'pct-mid'
  return 'pct-bad'
}

function PctBar({ pct }) {
  return (
    <div className="pct-bar-wrap">
      <div className={`pct-bar ${pctClass(pct)}`} style={{ width: `${Math.min(100, pct)}%` }} />
      <span className={`pct-label ${pctClass(pct)}`}>{pct}%</span>
    </div>
  )
}

/**
 * Color-coded per-subject attendance. Self-service when no studentId given
 * (logged-in student), otherwise reads the named student (parent/staff view).
 */
export default function AttendanceViewPage({ studentId, title = 'My Attendance', readOnly = false }) {
  const [summary, setSummary] = useState([])
  const [history, setHistory] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const calls = studentId
      ? [getStudentAttendanceSummary(studentId), getStudentAttendance(studentId)]
      : [getMyAttendanceSummary(), getMyAttendance()]
    Promise.all(calls)
      .then(([s, h]) => { setSummary(s.data); setHistory(h.data) })
      .catch((e) => setError(pickErrorMessage(e, 'Failed to load attendance')))
      .finally(() => setLoading(false))
  }, [studentId])

  const overall = summary.length
    ? Math.round(summary.reduce((a, s) => a + s.percentage, 0) / summary.length)
    : 0

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>{title}</h1>
          <p className="muted">EXCUSED sessions are not counted against the percentage</p>
        </div>
        <div className={`overall-pct ${pctClass(overall)}`}>{overall}% overall</div>
      </div>

      {error && <div className="form-error">{error}</div>}
      {loading ? <div className="state-box"><span className="spinner" /> Loading…</div> : (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th><th>Subject</th><th>Total</th><th>Present</th>
                  <th>Late</th><th>Absent</th><th>Excused</th><th>Percentage</th>
                </tr>
              </thead>
              <tbody>
                {summary.map((s) => (
                  <tr key={s.subjectId}>
                    <td>{s.subjectCode}</td>
                    <td>{s.subjectName}</td>
                    <td>{s.total}</td>
                    <td className="txt-good">{s.present}</td>
                    <td className="txt-mid">{s.late}</td>
                    <td className="txt-bad">{s.absent}</td>
                    <td className="muted">{s.excused}</td>
                    <td style={{ minWidth: 180 }}><PctBar pct={s.percentage} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {summary.length === 0 && <div className="state-box">📭 No attendance recorded yet.</div>}

          {!readOnly && (
            <>
              <h3 style={{ margin: '1.6rem 0 .6rem' }}>Recent sessions</h3>
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr><th>Date</th><th>Subject</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {history.slice(0, 25).map((h) => (
                      <tr key={h.id}>
                        <td>{h.attendanceDate}</td>
                        <td>{h.subjectName}</td>
                        <td><StatusPillInline status={h.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      )}
    </div>
  )
}

function StatusPillInline({ status }) {
  const cls = { PRESENT: 'att-present', ABSENT: 'att-absent', LATE: 'att-late', EXCUSED: 'att-excused' }[status] || ''
  return <span className={`att-pill ${cls}`}>{status}</span>
}
