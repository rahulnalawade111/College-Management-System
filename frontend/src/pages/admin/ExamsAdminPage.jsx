import { useEffect, useState } from 'react'
import examService from '../../services/examService'
import { academicYearService, semesterService, subjectService } from '../../services/masterDataService'
import { SectionCard } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'
import Modal from '../../components/Modal'

const EMPTY_EXAM = { examName: '', examType: 'MIDTERM', academicYearId: '', semesterId: '', startDate: '', endDate: '' }
const EMPTY_SLOT = { subjectId: '', examDate: '', startTime: '09:30', endTime: '12:30', room: '', maxMarks: 50 }

function fmtDate(d) {
  return d ? new Date(d).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'
}
function fmtTime(t) {
  return t ? t.slice(0, 5) : '—'
}

export default function ExamsAdminPage() {
  const toast = useToast()
  const [exams, setExams] = useState(null)
  const [years, setYears] = useState([])
  const [semesters, setSemesters] = useState([])
  const [subjects, setSubjects] = useState([])
  const [error, setError] = useState(null)
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(EMPTY_EXAM)
  const [saving, setSaving] = useState(false)
  const [slotExam, setSlotExam] = useState(null) // exam getting a new schedule slot
  const [slotForm, setSlotForm] = useState(EMPTY_SLOT)

  const load = () => examService.all().then(setExams)
    .catch(e => setError(e.response?.data?.message || 'Failed to load exams'))

  useEffect(() => {
    load()
    academicYearService.list().then(setYears).catch(() => {})
    semesterService.list().then(setSemesters).catch(() => {})
    subjectService.list().then(setSubjects).catch(() => {})
  }, [])

  async function createExam(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await examService.create({
        ...form,
        academicYearId: Number(form.academicYearId),
        semesterId: form.semesterId ? Number(form.semesterId) : null,
      })
      toast.success('Exam created')
      setShowCreate(false)
      setForm(EMPTY_EXAM)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create exam')
    } finally {
      setSaving(false)
    }
  }

  async function changeStatus(exam, status) {
    try {
      await examService.setStatus(exam.id, status)
      toast.success(`Exam marked ${status.toLowerCase()}`)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update status')
    }
  }

  async function addSlot(e) {
    e.preventDefault()
    try {
      await examService.addSchedule(slotExam.id, {
        subjectId: Number(slotForm.subjectId),
        examDate: slotForm.examDate,
        startTime: slotForm.startTime,
        endTime: slotForm.endTime,
        room: slotForm.room || null,
        maxMarks: Number(slotForm.maxMarks),
      })
      toast.success('Schedule slot added')
      setSlotExam(await examService.one(slotExam.id))
      setSlotForm(EMPTY_SLOT)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add slot')
    }
  }

  async function removeSlot(scheduleId) {
    try {
      await examService.removeSchedule(slotExam.id, scheduleId)
      toast.success('Slot removed')
      setSlotExam(await examService.one(slotExam.id))
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to remove slot')
    }
  }

  if (error) return <ErrorState message={error} />
  if (!exams) return <Loading label="Loading exams…" />

  return (
    <div className="dashboard-page">
      <div className="page-head-row">
        <h1 className="page-title">Exams & Schedules</h1>
        <button type="button" className="btn btn-primary" onClick={() => setShowCreate(true)}>+ New Exam</button>
      </div>

      {exams.length ? exams.map(exam => (
        <SectionCard key={exam.id}
          title={`${exam.examName} · ${exam.examType}`}
          actions={
            <div className="btn-row">
              <StatusBadge value={exam.status} />
              {exam.status === 'SCHEDULED' &&
                <button type="button" className="btn btn-sm btn-ghost" onClick={() => changeStatus(exam, 'ONGOING')}>Start</button>}
              {exam.status === 'ONGOING' &&
                <button type="button" className="btn btn-sm btn-ghost" onClick={() => changeStatus(exam, 'COMPLETED')}>Complete</button>}
              {exam.status !== 'CANCELLED' && exam.status !== 'COMPLETED' &&
                <button type="button" className="btn btn-sm btn-ghost" onClick={() => changeStatus(exam, 'CANCELLED')}>Cancel</button>}
              <button type="button" className="btn btn-sm btn-primary"
                onClick={() => { setSlotExam(exam); setSlotForm({ ...EMPTY_SLOT, examDate: exam.startDate || '' }) }}>
                + Slot
              </button>
            </div>
          }>
          <p className="muted small">
            {exam.academicYearName}{exam.semesterNumber ? ` · Sem ${exam.semesterNumber}` : ''}
            {' · '}{fmtDate(exam.startDate)} → {fmtDate(exam.endDate)}
          </p>
          {exam.schedule?.length ? (
            <table className="table">
              <thead>
                <tr><th>Date</th><th>Time</th><th>Subject</th><th>Room</th><th>Max</th></tr>
              </thead>
              <tbody>
                {exam.schedule.map(s => (
                  <tr key={s.id}>
                    <td>{fmtDate(s.examDate)}</td>
                    <td className="small">{fmtTime(s.startTime)} – {fmtTime(s.endTime)}</td>
                    <td><strong>{s.subjectCode}</strong> <span className="muted small">{s.subjectName}</span></td>
                    <td className="small">{s.room || '—'}</td>
                    <td>{s.maxMarks}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <Empty message="No schedule slots yet for this exam" />}
        </SectionCard>
      )) : <Empty message="No exams yet. Create the first one!" />}

      {showCreate && (
        <Modal title="New Exam" onClose={() => setShowCreate(false)}>
          <form onSubmit={createExam} className="form-grid">
            <label>Exam name *
              <input required maxLength={120} value={form.examName}
                onChange={e => setForm({ ...form, examName: e.target.value })} />
            </label>
            <div className="form-row-2">
              <label>Type
                <select value={form.examType}
                  onChange={e => setForm({ ...form, examType: e.target.value })}>
                  <option>INTERNAL</option><option>MIDTERM</option>
                  <option>FINAL</option><option>PRACTICAL</option>
                </select>
              </label>
              <label>Academic year *
                <select required value={form.academicYearId}
                  onChange={e => setForm({ ...form, academicYearId: e.target.value })}>
                  <option value="">Select…</option>
                  {years.map(y => <option key={y.id} value={y.id}>{y.yearName}</option>)}
                </select>
              </label>
            </div>
            <label>Semester (optional)
              <select value={form.semesterId}
                onChange={e => setForm({ ...form, semesterId: e.target.value })}>
                <option value="">All semesters</option>
                {semesters.map(s => <option key={s.id} value={s.id}>{s.semesterName || `Semester ${s.semesterNumber}`}</option>)}
              </select>
            </label>
            <div className="form-row-2">
              <label>Start date *
                <input type="date" required value={form.startDate}
                  onChange={e => setForm({ ...form, startDate: e.target.value })} />
              </label>
              <label>End date *
                <input type="date" required value={form.endDate}
                  onChange={e => setForm({ ...form, endDate: e.target.value })} />
              </label>
            </div>
            <div className="btn-row end">
              <button type="button" className="btn btn-ghost" onClick={() => setShowCreate(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Create'}</button>
            </div>
          </form>
        </Modal>
      )}

      {slotExam && (
        <Modal title={`Add slot — ${slotExam.examName}`} onClose={() => setSlotExam(null)}>
          <form onSubmit={addSlot} className="form-grid">
            <label>Subject *
              <select required value={slotForm.subjectId}
                onChange={e => setSlotForm({ ...slotForm, subjectId: e.target.value })}>
                <option value="">Select…</option>
                {subjects.map(s => <option key={s.id} value={s.id}>{s.subjectCode} — {s.subjectName}</option>)}
              </select>
            </label>
            <div className="form-row-2">
              <label>Date *
                <input type="date" required min={slotExam.startDate} max={slotExam.endDate}
                  value={slotForm.examDate}
                  onChange={e => setSlotForm({ ...slotForm, examDate: e.target.value })} />
              </label>
              <label>Max marks
                <input type="number" min="1" value={slotForm.maxMarks}
                  onChange={e => setSlotForm({ ...slotForm, maxMarks: e.target.value })} />
              </label>
            </div>
            <div className="form-row-2">
              <label>Start time *
                <input type="time" required value={slotForm.startTime}
                  onChange={e => setSlotForm({ ...slotForm, startTime: e.target.value })} />
              </label>
              <label>End time *
                <input type="time" required value={slotForm.endTime}
                  onChange={e => setSlotForm({ ...slotForm, endTime: e.target.value })} />
              </label>
            </div>
            <label>Room
              <input maxLength={50} value={slotForm.room} placeholder="Room 101"
                onChange={e => setSlotForm({ ...slotForm, room: e.target.value })} />
            </label>
            <div className="btn-row end">
              <button type="button" className="btn btn-ghost" onClick={() => setSlotExam(null)}>Done</button>
              <button type="submit" className="btn btn-primary">Add slot</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
