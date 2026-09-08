import { useEffect, useState, useCallback } from 'react'
import { courseService, departmentService } from '../../services/masterDataService'
import { useToast } from '../../context/ToastContext'
import DataTable from '../../components/DataTable'
import Pagination from '../../components/Pagination'
import Modal from '../../components/Modal'
import StatusBadge from '../../components/StatusBadge'
import { pickErrorMessage } from '../../components/States'

const EMPTY = { courseCode: '', courseName: '', description: '', duration: '3 Years', degreeType: 'UG', departmentId: '', totalSemesters: 6, fees: 0, status: 'ACTIVE' }

export default function CoursesPage() {
  const toast = useToast()
  const [data, setData] = useState(null)
  const [departments, setDepartments] = useState([])
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [sort, setSort] = useState({ field: 'courseName', dir: 'asc' })
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setError(null)
    try {
      const params = { page, size, sort: `${sort.field},${sort.dir}` }
      if (search) params.search = search
      if (departmentId) params.departmentId = departmentId
      if (status) params.status = status
      setData(await courseService.list(params))
    } catch (err) {
      setError(pickErrorMessage(err, 'Failed to load courses'))
    }
  }, [page, size, sort, search, departmentId, status])

  useEffect(() => { load() }, [load])
  useEffect(() => {
    departmentService.list().then(setDepartments).catch(() => {})
  }, [])

  function openCreate() { setForm(EMPTY); setModal({ mode: 'create' }) }
  function openEdit(row) {
    setForm({
      courseCode: row.courseCode, courseName: row.courseName, description: row.description || '',
      duration: row.duration, degreeType: row.degreeType, departmentId: String(row.departmentId),
      totalSemesters: row.totalSemesters, fees: row.fees, status: row.status,
    })
    setModal({ mode: 'edit', row })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = { ...form, departmentId: Number(form.departmentId), totalSemesters: Number(form.totalSemesters), fees: Number(form.fees) }
      if (modal.mode === 'create') {
        await courseService.create(payload)
        toast.success('Course created')
      } else {
        await courseService.update(modal.row.id, payload)
        toast.success('Course updated')
      }
      setModal(null)
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Save failed'))
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete(row) {
    if (!window.confirm(`Delete course "${row.courseCode} — ${row.courseName}"?`)) return
    try {
      await courseService.remove(row.id)
      toast.success('Course deleted')
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Delete failed'))
    }
  }

  const columns = [
    { key: 'courseCode', label: 'Code', sortable: true },
    { key: 'courseName', label: 'Course', sortable: true },
    { key: 'departmentName', label: 'Department', sortable: true },
    { key: 'degreeType', label: 'Degree' },
    { key: 'duration', label: 'Duration' },
    { key: 'totalSemesters', label: 'Sems' },
    { key: 'fees', label: 'Fees', render: (r) => `₹${Number(r.fees).toLocaleString('en-IN')}` },
    { key: 'status', label: 'Status', render: (r) => <StatusBadge value={r.status} /> },
    {
      key: 'actions', label: 'Actions',
      render: (r) => (
        <div className="row-actions">
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => openEdit(r)}>Edit</button>
          <button type="button" className="btn btn-ghost btn-sm danger" onClick={() => handleDelete(r)}>Delete</button>
        </div>
      ),
    },
  ]

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h1>Courses</h1>
          <p className="muted">{data?.totalElements ?? '…'} courses</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={openCreate}>+ Add Course</button>
      </div>

      <div className="filters">
        <input className="filter-search" placeholder="Search name or code…" value={search}
               onChange={(e) => { setPage(0); setSearch(e.target.value) }} />
        <select value={departmentId} onChange={(e) => { setPage(0); setDepartmentId(e.target.value) }}>
          <option value="">All departments</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
        </select>
        <select value={status} onChange={(e) => { setPage(0); setStatus(e.target.value) }}>
          <option value="">All statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
          <option value="ARCHIVED">Archived</option>
        </select>
      </div>

      <div className="card">
        <DataTable columns={columns} rows={data?.content || []}
                   loading={data === null && !error} error={error} onRetry={load}
                   emptyMessage="No courses found." emptyHint="Try clearing filters or add a course."
                   sort={sort} onSortChange={(s) => setSort(s)} />
        {data && (
          <Pagination page={data.number} totalPages={data.totalPages} totalElements={data.totalElements}
                      size={data.size} onPageChange={setPage} onSizeChange={(n) => { setSize(n); setPage(0) }} />
        )}
      </div>

      {modal && (
        <Modal title={modal.mode === 'create' ? 'Add Course' : 'Edit Course'} onClose={() => setModal(null)} wide>
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-row">
              <div className="form-field">
                <label>Course code</label>
                <input value={form.courseCode} required maxLength={20}
                       onChange={(e) => setForm({ ...form, courseCode: e.target.value.toUpperCase() })} placeholder="e.g. BCA" />
              </div>
              <div className="form-field">
                <label>Degree type</label>
                <select value={form.degreeType} onChange={(e) => setForm({ ...form, degreeType: e.target.value })}>
                  <option>UG</option><option>PG</option><option>DIPLOMA</option><option>CERTIFICATE</option>
                </select>
              </div>
            </div>
            <div className="form-field">
              <label>Course name</label>
              <input value={form.courseName} required maxLength={120}
                     onChange={(e) => setForm({ ...form, courseName: e.target.value })} />
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Department</label>
                <select value={form.departmentId} required onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
                  <option value="">Select…</option>
                  {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
                </select>
              </div>
              <div className="form-field">
                <label>Duration</label>
                <input value={form.duration} required onChange={(e) => setForm({ ...form, duration: e.target.value })} placeholder="3 Years" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Total semesters</label>
                <input type="number" min={1} max={12} value={form.totalSemesters} required
                       onChange={(e) => setForm({ ...form, totalSemesters: e.target.value })} />
              </div>
              <div className="form-field">
                <label>Fees (₹/year)</label>
                <input type="number" min={0} step="0.01" value={form.fees} required
                       onChange={(e) => setForm({ ...form, fees: e.target.value })} />
              </div>
            </div>
            <div className="form-field">
              <label>Status</label>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option>ACTIVE</option><option>INACTIVE</option><option>ARCHIVED</option>
              </select>
            </div>
            <div className="form-field">
              <label>Description</label>
              <textarea rows={3} value={form.description} maxLength={500}
                        onChange={(e) => setForm({ ...form, description: e.target.value })} />
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
