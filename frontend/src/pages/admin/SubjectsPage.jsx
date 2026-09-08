import { useEffect, useState, useCallback } from 'react'
import { subjectService, courseService, departmentService, semesterService, academicYearService } from '../../services/masterDataService'
import { useToast } from '../../context/ToastContext'
import DataTable from '../../components/DataTable'
import Pagination from '../../components/Pagination'
import Modal from '../../components/Modal'
import { pickErrorMessage } from '../../components/States'

const EMPTY = { subjectCode: '', subjectName: '', credits: 4, semesterId: '', courseId: '', departmentId: '', description: '' }

export default function SubjectsPage() {
  const toast = useToast()
  const [data, setData] = useState(null)
  const [meta, setMeta] = useState({ courses: [], departments: [], semesters: [], years: [] })
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [courseId, setCourseId] = useState('')
  const [yearId, setYearId] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [sort, setSort] = useState({ field: 'subjectCode', dir: 'asc' })
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setError(null)
    try {
      const params = { page, size, sort: `${sort.field},${sort.dir}` }
      if (search) params.search = search
      if (courseId) params.courseId = courseId
      setData(await subjectService.list(params))
    } catch (err) {
      setError(pickErrorMessage(err, 'Failed to load subjects'))
    }
  }, [page, size, sort, search, courseId])

  useEffect(() => { load() }, [load])

  useEffect(() => {
    Promise.all([courseService.list({ page: 0, size: 100 }), departmentService.list(), semesterService.list(), academicYearService.list()])
      .then(([courses, departments, semesters, years]) =>
        setMeta({
          courses: courses.content || [],
          departments,
          semesters,
          years,
        }))
      .catch(() => {})
  }, [])

  const semestersForYear = meta.semesters.filter((s) => !yearId || String(s.academicYearId) === yearId)

  function openCreate() { setForm(EMPTY); setModal({ mode: 'create' }) }
  function openEdit(row) {
    setForm({
      subjectCode: row.subjectCode, subjectName: row.subjectName, credits: row.credits,
      semesterId: String(row.semesterId), courseId: String(row.courseId),
      departmentId: String(row.departmentId), description: row.description || '',
    })
    setModal({ mode: 'edit', row })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = {
        ...form,
        credits: Number(form.credits),
        semesterId: Number(form.semesterId),
        courseId: Number(form.courseId),
        departmentId: Number(form.departmentId),
      }
      if (modal.mode === 'create') {
        await subjectService.create(payload)
        toast.success('Subject created')
      } else {
        await subjectService.update(modal.row.id, payload)
        toast.success('Subject updated')
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
    if (!window.confirm(`Delete subject "${row.subjectCode} — ${row.subjectName}"?`)) return
    try {
      await subjectService.remove(row.id)
      toast.success('Subject deleted')
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Delete failed'))
    }
  }

  const columns = [
    { key: 'subjectCode', label: 'Code', sortable: true },
    { key: 'subjectName', label: 'Subject', sortable: true },
    { key: 'courseCode', label: 'Course' },
    { key: 'semesterName', label: 'Semester' },
    { key: 'departmentName', label: 'Department' },
    { key: 'credits', label: 'Credits' },
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
          <h1>Subjects</h1>
          <p className="muted">{data?.totalElements ?? '…'} subjects</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={openCreate}>+ Add Subject</button>
      </div>

      <div className="filters">
        <input className="filter-search" placeholder="Search name or code…" value={search}
               onChange={(e) => { setPage(0); setSearch(e.target.value) }} />
        <select value={courseId} onChange={(e) => { setPage(0); setCourseId(e.target.value) }}>
          <option value="">All courses</option>
          {meta.courses.map((c) => <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>)}
        </select>
        <select value={yearId} onChange={() => {}}>
          <option value="">All years</option>
          {meta.years.map((y) => <option key={y.id} value={y.id}>{y.yearName}</option>)}
        </select>
      </div>

      <div className="card">
        <DataTable columns={columns} rows={data?.content || []}
                   loading={data === null && !error} error={error} onRetry={load}
                   emptyMessage="No subjects found." emptyHint="Add subjects or clear the filters."
                   sort={sort} onSortChange={setSort} />
        {data && (
          <Pagination page={data.number} totalPages={data.totalPages} totalElements={data.totalElements}
                      size={data.size} onPageChange={setPage} onSizeChange={(n) => { setSize(n); setPage(0) }} />
        )}
      </div>

      {modal && (
        <Modal title={modal.mode === 'create' ? 'Add Subject' : 'Edit Subject'} onClose={() => setModal(null)} wide>
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-row">
              <div className="form-field">
                <label>Subject code</label>
                <input value={form.subjectCode} required maxLength={20}
                       onChange={(e) => setForm({ ...form, subjectCode: e.target.value.toUpperCase() })} placeholder="e.g. BCA301" />
              </div>
              <div className="form-field">
                <label>Credits</label>
                <input type="number" min={1} max={10} value={form.credits} required
                       onChange={(e) => setForm({ ...form, credits: e.target.value })} />
              </div>
            </div>
            <div className="form-field">
              <label>Subject name</label>
              <input value={form.subjectName} required maxLength={120}
                     onChange={(e) => setForm({ ...form, subjectName: e.target.value })} />
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Course</label>
                <select value={form.courseId} required onChange={(e) => setForm({ ...form, courseId: e.target.value })}>
                  <option value="">Select…</option>
                  {meta.courses.map((c) => <option key={c.id} value={c.id}>{c.courseCode}</option>)}
                </select>
              </div>
              <div className="form-field">
                <label>Department</label>
                <select value={form.departmentId} required onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
                  <option value="">Select…</option>
                  {meta.departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
                </select>
              </div>
            </div>
            <div className="form-field">
              <label>Semester</label>
              <select value={form.semesterId} required onChange={(e) => setForm({ ...form, semesterId: e.target.value })}>
                <option value="">Select…</option>
                {meta.semesters.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.academicYearName} · {s.semesterName}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-field">
              <label>Description</label>
              <textarea rows={2} value={form.description} maxLength={500}
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
