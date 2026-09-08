import { useEffect, useState, useCallback } from 'react'
import {
  listStudents, getStudent, createStudent, updateStudent, updateStudentStatus,
  deleteStudent, listDepartments, listCourses, listSemesters, listAcademicYears,
} from '../../services/peopleService'
import DataTable from '../../components/DataTable'
import Pagination from '../../components/Pagination'
import StatusBadge from '../../components/StatusBadge'
import Modal from '../../components/Modal'
import { Loading, Empty as EmptyState, ErrorState } from '../../components/States'
import { useToast } from '../../context/ToastContext'

const STATUSES = ['ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED', 'TRANSFERRED']

const EMPTY = {
  studentId: '', firstName: '', lastName: '', email: '', phone: '', dateOfBirth: '',
  gender: '', addressLine1: '', city: '', state: '', pincode: '', admissionDate: '',
  courseId: '', departmentId: '', semesterId: '', academicYearId: '', status: 'ACTIVE',
  guardians: [{ name: '', relation: 'FATHER', email: '', phone: '', occupation: '', address: '' }],
}

export default function StudentsPage() {
  const { success, error } = useToast()
  const [rows, setRows] = useState([])
  const [page, setPage] = useState({ number: 0, totalPages: 0, totalElements: 0 })
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [search, setSearch] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [courseId, setCourseId] = useState('')
  const [status, setStatus] = useState('')
  const [pageSize, setPageSize] = useState(10)

  const [departments, setDepartments] = useState([])
  const [courses, setCourses] = useState([])
  const [semesters, setSemesters] = useState([])
  const [years, setYears] = useState([])

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [csv, setCsv] = useState('')

  const load = useCallback(async (p = 0) => {
    setLoading(true)
    setLoadError('')
    try {
      const params = { page: p, size: pageSize }
      if (search) params.search = search
      if (departmentId) params.departmentId = departmentId
      if (courseId) params.courseId = courseId
      if (status) params.status = status
      const { data } = await listStudents(params)
      setRows(data.content || [])
      setPage({ number: data.number, totalPages: data.totalPages, totalElements: data.totalElements })
    } catch (e) {
      setLoadError(e.response?.data?.message || 'Failed to load students')
    } finally {
      setLoading(false)
    }
  }, [search, departmentId, courseId, status, pageSize])

  useEffect(() => { load(0) }, [load])

  useEffect(() => {
    Promise.all([listDepartments(), listCourses({ size: 100 }), listSemesters(), listAcademicYears()])
      .then(([d, c, s, y]) => {
        setDepartments(d.data)
        setCourses(c.data.content || c.data)
        setSemesters(Array.isArray(s.data) ? s.data : s.data.content || [])
        setYears(y.data)
      }).catch(() => {})
  }, [])

  const exportCsv = () => {
    const header = ['Student ID', 'Name', 'Email', 'Department', 'Course', 'Semester', 'Status']
    const lines = rows.map((r) => [
      r.studentId, r.fullName, r.email, r.departmentName, r.courseName, r.semesterName, r.status,
    ].map((v) => `"${(v ?? '').toString().replace(/"/g, '""')}"`).join(','))
    setCsv([header.join(','), ...lines].join('\n'))
  }

  useEffect(() => {
    if (!csv) return
    const blob = new Blob([csv], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `students-page-${page.number + 1}.csv`
    a.click()
    URL.revokeObjectURL(url)
    setCsv('')
  }, [csv, page.number])

  const openCreate = () => {
    setEditing(null)
    setForm({
      ...EMPTY,
      academicYearId: years.find((y) => y.active)?.id?.toString() || '',
      guardians: [{ name: '', relation: 'FATHER', email: '', phone: '', occupation: '', address: '' }],
    })
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = async (s) => {
    try {
      const { data } = await getStudent(s.id)
      setEditing(data)
      setForm({
        studentId: data.studentId, firstName: data.firstName, lastName: data.lastName,
        email: data.email, phone: data.phone || '', dateOfBirth: data.dateOfBirth || '',
        gender: data.gender || '', addressLine1: data.addressLine1 || '',
        city: data.city || '', state: data.state || '', pincode: data.pincode || '',
        admissionDate: data.admissionDate || '',
        courseId: data.courseId?.toString() || '', departmentId: data.departmentId?.toString() || '',
        semesterId: data.semesterId?.toString() || '', academicYearId: data.academicYearId?.toString() || '',
        status: data.status || 'ACTIVE',
        guardians: (data.guardians?.length ? data.guardians : [{ name: '', relation: 'FATHER', email: '', phone: '', occupation: '', address: '' }])
          .map((g) => ({ name: g.name || '', relation: g.relation || 'FATHER', email: g.email || '', phone: g.phone || '', occupation: g.occupation || '', address: g.address || '' })),
      })
      setFormError('')
      setModalOpen(true)
    } catch (e) {
      error('Could not load student details')
    }
  }

  const save = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      const payload = {
        ...form,
        courseId: Number(form.courseId),
        departmentId: Number(form.departmentId),
        semesterId: Number(form.semesterId),
        academicYearId: Number(form.academicYearId),
        dateOfBirth: form.dateOfBirth || null,
        admissionDate: form.admissionDate || null,
        gender: form.gender || null,
        guardians: form.guardians.filter((g) => g.name.trim()),
      }
      if (editing) {
        await updateStudent(editing.id, payload)
        success('Student updated')
      } else {
        await createStudent(payload)
        success('Student added')
      }
      setModalOpen(false)
      load(page.number)
    } catch (err) {
      setFormError(err.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  const changeStatus = async (s, newStatus) => {
    try {
      await updateStudentStatus(s.id, newStatus)
      success(`${s.fullName} → ${newStatus}`)
      load(page.number)
    } catch (err) {
      error(err.response?.data?.message || 'Status change failed')
    }
  }

  const remove = async (s) => {
    if (!window.confirm(`Delete student ${s.fullName}?`)) return
    try {
      await deleteStudent(s.id)
      success('Student deleted')
      load(page.number)
    } catch (err) {
      error(err.response?.data?.message || 'Delete failed')
    }
  }

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))
  const setGuardian = (i, k) => (e) => setForm((f) => ({
    ...f,
    guardians: f.guardians.map((g, gi) => (gi === i ? { ...g, [k]: e.target.value } : g)),
  }))

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Students</h1>
          <p className="muted">{page.totalElements} enrolled</p>
        </div>
        <div style={{ display: 'flex', gap: '.6rem' }}>
          <button className="btn btn-outline-dark" onClick={exportCsv}>⬇ CSV</button>
          <button className="btn" onClick={openCreate}>+ Add Student</button>
        </div>
      </div>

      <div className="filters">
        <input className="filter-search" placeholder="Search name, student ID or email…"
          value={search} onChange={(e) => setSearch(e.target.value)} />
        <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
          <option value="">All departments</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
        </select>
        <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
          <option value="">All courses</option>
          {courses.map((c) => <option key={c.id} value={c.id}>{c.courseName}</option>)}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>

      {loading ? <Loading /> : loadError ? <ErrorState message={loadError} onRetry={() => load(page.number)} /> :
        rows.length === 0 ? <EmptyState message="No students match the filters" /> : (
          <>
            <DataTable
              columns={[
                { key: 'studentId', label: 'Student ID' },
                { key: 'fullName', label: 'Name' },
                { key: 'email', label: 'Email' },
                { key: 'departmentName', label: 'Department' },
                { key: 'courseName', label: 'Course' },
                { key: 'semesterName', label: 'Semester' },
                { key: 'status', label: 'Status', render: (r) => <StatusBadge status={r.status} /> },
              ]}
              rows={rows}
              actions={(r) => (
                <span className="row-actions">
                  <select
                    className="page-size" value={r.status}
                    onChange={(e) => changeStatus(r, e.target.value)}
                    title="Change status"
                  >
                    {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                  <button className="btn btn-sm" onClick={() => openEdit(r)}>Edit</button>
                  <button className="btn btn-sm danger" onClick={() => remove(r)}>Delete</button>
                </span>
              )}
            />
            <Pagination
              page={page.number} totalPages={page.totalPages} totalElements={page.totalElements}
              onPage={load} pageSize={pageSize}
              onPageSize={setPageSize}
            />
          </>
        )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} wide
        title={editing ? `Edit ${editing.fullName}` : 'Add Student'}>
        <form onSubmit={save}>
          {formError && <div className="form-error">{formError}</div>}

          <h4 className="form-section">Academic</h4>
          <div className="form-row">
            <div className="form-group">
              <label>Student ID *</label>
              <input required value={form.studentId} onChange={set('studentId')} placeholder="ABC2026CS001" />
            </div>
            <div className="form-group">
              <label>Admission date</label>
              <input type="date" value={form.admissionDate} onChange={set('admissionDate')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Course *</label>
              <select required value={form.courseId} onChange={set('courseId')}>
                <option value="">Select…</option>
                {courses.map((c) => <option key={c.id} value={c.id}>{c.courseName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Department *</label>
              <select required value={form.departmentId} onChange={set('departmentId')}>
                <option value="">Select…</option>
                {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
              </select>
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Semester *</label>
              <select required value={form.semesterId} onChange={set('semesterId')}>
                <option value="">Select…</option>
                {semesters.map((s) => <option key={s.id} value={s.id}>{s.semesterName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Academic year *</label>
              <select required value={form.academicYearId} onChange={set('academicYearId')}>
                <option value="">Select…</option>
                {years.map((y) => <option key={y.id} value={y.id}>{y.yearName}{y.active ? ' (active)' : ''}</option>)}
              </select>
            </div>
          </div>

          <h4 className="form-section">Personal</h4>
          <div className="form-row">
            <div className="form-group">
              <label>First name *</label>
              <input required value={form.firstName} onChange={set('firstName')} />
            </div>
            <div className="form-group">
              <label>Last name *</label>
              <input required value={form.lastName} onChange={set('lastName')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Email *</label>
              <input required type="email" value={form.email} onChange={set('email')} />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input value={form.phone} onChange={set('phone')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Date of birth</label>
              <input type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} />
            </div>
            <div className="form-group">
              <label>Gender</label>
              <select value={form.gender} onChange={set('gender')}>
                <option value="">—</option>
                <option>MALE</option><option>FEMALE</option><option>OTHER</option>
              </select>
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Status</label>
              <select value={form.status} onChange={set('status')}>
                {STATUSES.map((s) => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-group" />
          </div>

          <h4 className="form-section">Address</h4>
          <div className="form-group">
            <label>Address line</label>
            <input value={form.addressLine1} onChange={set('addressLine1')} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>City</label>
              <input value={form.city} onChange={set('city')} />
            </div>
            <div className="form-group">
              <label>State</label>
              <input value={form.state} onChange={set('state')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Pincode</label>
              <input value={form.pincode} onChange={set('pincode')} />
            </div>
            <div className="form-group" />
          </div>

          <h4 className="form-section">Guardians</h4>
          {form.guardians.map((g, i) => (
            <div key={i} className="guardian-block">
              <div className="form-row">
                <div className="form-group">
                  <label>Name</label>
                  <input value={g.name} onChange={setGuardian(i, 'name')} />
                </div>
                <div className="form-group">
                  <label>Relation</label>
                  <select value={g.relation} onChange={setGuardian(i, 'relation')}>
                    <option>FATHER</option><option>MOTHER</option><option>GUARDIAN</option>
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Email</label>
                  <input type="email" value={g.email} onChange={setGuardian(i, 'email')} />
                </div>
                <div className="form-group">
                  <label>Phone</label>
                  <input value={g.phone} onChange={setGuardian(i, 'phone')} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Occupation</label>
                  <input value={g.occupation} onChange={setGuardian(i, 'occupation')} />
                </div>
                <div className="form-group">
                  <label>Address</label>
                  <input value={g.address} onChange={setGuardian(i, 'address')} />
                </div>
              </div>
              {form.guardians.length > 1 && (
                <button type="button" className="btn btn-sm btn-outline-dark"
                  onClick={() => setForm((f) => ({ ...f, guardians: f.guardians.filter((_, gi) => gi !== i) }))}>
                  Remove guardian
                </button>
              )}
            </div>
          ))}
          <button type="button" className="btn btn-sm btn-outline-dark"
            onClick={() => setForm((f) => ({
              ...f,
              guardians: [...f.guardians, { name: '', relation: 'GUARDIAN', email: '', phone: '', occupation: '', address: '' }],
            }))}>
            + Add another guardian
          </button>

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn" disabled={saving}>
              {saving ? 'Saving…' : editing ? 'Save changes' : 'Add student'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
