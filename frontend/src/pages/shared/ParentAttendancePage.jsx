import { useEffect, useState } from 'react'
import api from '../../services/api'
import AttendanceViewPage from './AttendanceViewPage'
import { pickErrorMessage } from '../../components/States'

/** Parent's view of the linked child's attendance. */
export default function ParentAttendancePage() {
  const [child, setChild] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/parent/me/child')
      .then(({ data }) => setChild(data))
      .catch((e) => setError(pickErrorMessage(e, 'Could not load child record')))
  }, [])

  if (error) return <div className="form-error">{error}</div>
  if (!child) return <div className="state-box"><span className="spinner" /> Loading…</div>

  return <AttendanceViewPage studentId={child.id} title={`Attendance — ${child.fullName} (${child.studentId})`} readOnly />
}
