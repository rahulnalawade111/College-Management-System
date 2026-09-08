import { useEffect, useState, useCallback } from 'react'
import { academicYearService, semesterService } from '../../services/masterDataService'
import { useToast } from '../../context/ToastContext'
import DataTable from '../../components/DataTable'
import Modal from '../../components/Modal'
import StatusBadge from '../../components/StatusBadge'
import { pickErrorMessage } from '../../components/States'

const EMPTY_YEAR = { yearName: '', startDate: '', endDate: '', active: false }
const EMPTY_SEM = { semesterNumber: 1, semesterName: '', academicYearId: '' }

export default function AcademicYearsPage() {
  const toast = useToast()
  const [years, setYears] = useState(null)
  const [semesters, setSemesters] = useState(null)
  const [error, setError] = useState(null)
  const [tab, setTab] = useState('years')
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState(EMPTY_YEAR)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setError(null)
    try {
      const [y, s] = await Promise.all([academicYearService.list(), semesterService.list()])
      setYears(y)
      setSemesters(s)
    } catch (err) {
      setError(pickErrorMessage(err, 'Failed to load academic data'))
    }
  }, [])

  useEffect(() => { load() }, [load])

  async function handleSubmitYear(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = { ...form, startDate: form.startDate, endDate: form.endDate }
      if (modal.mode === 'create') {
        await academicYearService.create(payload)
        toast.success('Academic year created')
      } else {
        await academicYearService.update(modal.row.id, payload)
        toast.success('Academic year updated')
      }
      setModal(null)
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Save failed'))
    } finally {
      setBusy(false)
    }
  }

  async function handleSubmitSemester(e) {
    e.preventDefault()
    setBusy(true)
    try {
      if (modal.mode === 'create') {
        await semesterService.create({ ...form, academicYearId: Number(form.academicYearId) })
        toast.success('Semester created')
      } else {
        await semesterService.update(modal.row.id, { ...form, academicYearId: Number(form.academicYearId) })
        toast.success('Semester updated')
      }
      setModal(null)
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Save failed'))
    } finally {
      setBusy(false)
    }
  }

  async function activateYear(row) {
    try {
      await academicYearService.activate(row.id)
      toast.success(`${row.yearName} is now the active year`)
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Activation failed'))
    }
  }

  async function remove(item, kind) {
    if (!window.confirm(`Delete ${kind} "${item.yearName || item.semesterName}"?`)) return
    try {
      if (kind === 'year') await academicYearService.remove(item.id)
      else await semesterService.remove(item.id)
      toast.success('Deleted')
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Delete failed'))
    }
  }

  const yearColumns = [
    { key: 'yearName', label: 'Year', sortable: true },
    { key: 'startDate', label: 'Start' },
    { key: 'endDate', label: 'End' },
    { key: 'active', label: 'Status', render: (r) => <StatusBadge value={r.active ? 'ACTIVE' : 'INACTIVE'} /> },
    {
      key: 'actions', label: 'Actions',
      render: (r) => (
        <div className="row-actions">
          {!r.active && <button type="button" className="btn btn-ghost btn-sm" onClick={() => activateYear(r)}>Set active</button>}
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => {
            setForm({ yearName: r.yearName, startDate: r.startDate, endDate: r.endDate, active: r.active })
            setModal({ mode: 'edit', row: r, kind: 'year' })
          }}>Edit</button>
          <button type="button" className="btn btn-ghost btn-sm danger" onClick={() => remove(r, 'year')}>Delete</button>
        </div>
      ),
    },
  ]

  const semesterColumns = [
    { key: 'semesterName', label: 'Semester', sortable: true },
    { key: 'academicYearName', label: 'Academic Year' },
    { key: 'activeYear', label: 'Year Status', render: (r) => <StatusBadge value={r.activeYear ? 'ACTIVE' : 'INACTIVE'} /> },
    {
      key: 'actions', label: 'Actions',
      render: (r) => (
        <div className="row-actions">
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => {
            setForm({ semesterNumber: r.semesterNumber, semesterName: r.semesterName, academicYearId: String(r.academicYearId) })
            setModal({ mode: 'edit', row: r, kind: 'semester' })
          }}>Edit</button>
          <button type="button" className="btn btn-ghost btn-sm danger" onClick={() => remove(r, 'semester')}>Delete</button>
        </div>
      ),
    },
  ]

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h1>Academic Years & Semesters</h1>
          <p className="muted">Exactly one academic year is active at a time</p>
        </div>
        <div style={{ display: 'flex', gap: '.5rem' }}>
          <button type="button" className="btn btn-primary" onClick={() => {
            setForm(EMPTY_YEAR); setModal({ mode: 'create', kind: 'year' })
          }}>+ Add Year</button>
          <button type="button" className="btn btn-outline-dark" onClick={() => {
            setForm({ ...EMPTY_SEM, academicYearId: String(years?.find((y) => y.active)?.id ?? years?.[0]?.id ?? '') })
            setModal({ mode: 'create', kind: 'semester' })
          }}>+ Add Semester</button>
        </div>
      </div>

      <div className="tabs">
        <button className={tab === 'years' ? 'tab active' : 'tab'} onClick={() => setTab('years')}>Academic Years</button>
        <button className={tab === 'semesters' ? 'tab active' : 'tab'} onClick={() => setTab('semesters')}>Semesters</button>
      </div>

      <div className="card">
        {tab === 'years' ? (
          <DataTable columns={yearColumns} rows={years || []}
                     loading={years === null && !error} error={error} onRetry={load}
                     emptyMessage="No academic years found." />
        ) : (
          <DataTable columns={semesterColumns} rows={semesters || []}
                     loading={semesters === null && !error} error={error} onRetry={load}
                     emptyMessage="No semesters found." />
        )}
      </div>

      {modal?.kind === 'year' && (
        <Modal title={modal.mode === 'create' ? 'Add Academic Year' : 'Edit Academic Year'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmitYear} noValidate>
            <div className="form-field">
              <label>Year name</label>
              <input value={form.yearName} required placeholder="e.g. 2028-2029"
                     onChange={(e) => setForm({ ...form, yearName: e.target.value })} />
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Start date</label>
                <input type="date" value={form.startDate} required
                       onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
              </div>
              <div className="form-field">
                <label>End date</label>
                <input type="date" value={form.endDate} required
                       onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
              </div>
            </div>
            <label className="remember">
              <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
              Set as active year
            </label>
            <div className="modal-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setModal(null)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
            </div>
          </form>
        </Modal>
      )}

      {modal?.kind === 'semester' && (
        <Modal title={modal.mode === 'create' ? 'Add Semester' : 'Edit Semester'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmitSemester} noValidate>
            <div className="form-row">
              <div className="form-field">
                <label>Semester number</label>
                <input type="number" min={1} max={12} value={form.semesterNumber} required
                       onChange={(e) => setForm({ ...form, semesterNumber: Number(e.target.value) })} />
              </div>
              <div className="form-field">
                <label>Academic year</label>
                <select value={form.academicYearId} required
                        onChange={(e) => setForm({ ...form, academicYearId: e.target.value })}>
                  <option value="">Select…</option>
                  {(years || []).map((y) => <option key={y.id} value={y.id}>{y.yearName}</option>)}
                </select>
              </div>
            </div>
            <div className="form-field">
              <label>Name (optional — defaults to "Semester N")</label>
              <input value={form.semesterName}
                     onChange={(e) => setForm({ ...form, semesterName: e.target.value })} />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setModal(null)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
