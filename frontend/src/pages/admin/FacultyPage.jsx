import { useEffect, useState, useCallback } from 'react'
import { listFaculty, createFaculty, updateFaculty, deleteFaculty, listDepartments } from '../../services/peopleService'
import DataTable from '../../components/DataTable'
import Pagination from '../../components/Pagination'
import StatusBadge from '../../components/StatusBadge'
import Modal from '../../components/Modal'
import { Loading, Empty as EmptyState, ErrorState } from '../../components/States'
import { useToast } from '../../context/ToastContext'

const EMPTY_FORM = {
  employeeId: '', firstName: '', lastName: '', email: '', phone: '',
  dateOfBirth: '', gender: '', qualification: '', experienceYears: '',
  designation: '', departmentId: '', joiningDate: '', status: 'ACTIVE',
}

const FACULTY_STATUSES = ['ACTIVE', 'INACTIVE', 'ON_LEAVE', 'RETIRED']

export default function FacultyPage() {
  const { success, error } = useToast()
  const [rows, setRows] = useState([])
  const [page, setPage] = useState({ number: 0, totalPages: 0, totalElements: 0 })
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [search, setSearch] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [status, setStatus] = useState('')
  const [departments, setDepartments] = useState([])
  const [pageSize, setPageSize] = useState(10)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const load = useCallback(async (p = 0) => {
    setLoading(true)
    setLoadError('')
    try {
      const params = { page: p, size: pageSize }
      if (search) params.search = search
      if (departmentId) params.departmentId = departmentId
      if (status) params.status = status
      const { data } = await listFaculty(params)
      setRows(data.content || [])
      setPage({ number: data.number, totalPages: data.totalPages, totalElements: data.totalElements })
    } catch (e) {
      setLoadError(e.response?.data?.message || 'Failed to load faculty')
    } finally {
      setLoading(false)
    }
  }, [search, departmentId, status, pageSize])

  useEffect(() => { load(0) }, [load])
  useEffect(() => {
    listDepartments().then(({ data }) => setDepartments(data)).catch(() => {})
  }, [])

  const openCreate = () => {
    setEditing(null)
    setForm({ ...EMPTY_FORM, departmentId: departments[0]?.id?.toString() || '' })
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (f) => {
    setEditing(f)
    setForm({
      employeeId: f.employeeId || '', firstName: f.firstName || '', lastName: f.lastName || '',
      email: f.email || '', phone: f.phone || '',
      dateOfBirth: f.dateOfBirth || '', gender: f.gender || '',
      qualification: f.qualification || '',
      experienceYears: f.experienceYears ?? '',
      designation: f.designation || '',
      departmentId: f.departmentId?.toString() || '',
      joiningDate: f.joiningDate || '', status: f.status || 'ACTIVE',
    })
    setFormError('')
    setModalOpen(true)
  }

  const save = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      const payload = {
        ...form,
        experienceYears: form.experienceYears === '' ? null : Number(form.experienceYears),
        departmentId: Number(form.departmentId),
        dateOfBirth: form.dateOfBirth || null,
        joiningDate: form.joiningDate || null,
        gender: form.gender || null,
      }
      if (editing) {
        await updateFaculty(editing.id, payload)
        success('Faculty updated')
      } else {
        await createFaculty(payload)
        success('Faculty added')
      }
      setModalOpen(false)
      load(page.number)
    } catch (err) {
      setFormError(err.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  const remove = async (f) => {
    if (!window.confirm(`Delete faculty ${f.fullName}?`)) return
    try {
      await deleteFaculty(f.id)
      success('Faculty deleted')
      load(page.number)
    } catch (err) {
      error(err.response?.data?.message || 'Delete failed')
    }
  }

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Faculty</h1>
          <p className="muted">{page.totalElements} members</p>
        </div>
        <button className="btn" onClick={openCreate}>+ Add Faculty</button>
      </div>

      <div className="filters">
        <input
          className="filter-search" placeholder="Search name, employee ID or email…"
          value={search} onChange={(e) => setSearch(e.target.value)}
        />
        <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
          <option value="">All departments</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          {FACULTY_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {loading ? <Loading /> : loadError ? <ErrorState message={loadError} onRetry={() => load(page.number)} /> :
        rows.length === 0 ? <EmptyState message="No faculty match the filters" /> : (
          <>
            <DataTable
              columns={[
                { key: 'employeeId', label: 'Employee ID' },
                { key: 'fullName', label: 'Name' },
                { key: 'email', label: 'Email' },
                { key: 'designation', label: 'Designation' },
                { key: 'departmentName', label: 'Department' },
                { key: 'status', label: 'Status', render: (r) => <StatusBadge status={r.status} /> },
              ]}
              rows={rows}
              actions={(r) => (
                <span className="row-actions">
                  <button className="btn btn-sm" onClick={() => openEdit(r)}>Edit</button>
                  <button className="btn btn-sm danger" onClick={() => remove(r)}>Delete</button>
                </span>
              )}
            />
            <Pagination
              page={page.number} totalPages={page.totalPages} totalElements={page.totalElements}
              onPage={setPage2 => load(setPage2)} pageSize={pageSize}
              onPageSize={(s) => { setPageSize(s); }}
            />
          </>
        )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} wide
        title={editing ? `Edit ${editing.fullName}` : 'Add Faculty'}>
        <form onSubmit={save}>
          {formError && <div className="form-error">{formError}</div>}
          <div className="form-row">
            <div className="form-group">
              <label>Employee ID *</label>
              <input required value={form.employeeId} onChange={set('employeeId')} />
            </div>
            <div className="form-group">
              <label>Status</label>
              <select value={form.status} onChange={set('status')}>
                {FACULTY_STATUSES.map((s) => <option key={s}>{s}</option>)}
              </select>
            </div>
          </div>
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
              <label>Department *</label>
              <select required value={form.departmentId} onChange={set('departmentId')}>
                <option value="">Select…</option>
                {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Designation</label>
              <input value={form.designation} onChange={set('designation')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Qualification</label>
              <input value={form.qualification} onChange={set('qualification')} />
            </div>
            <div className="form-group">
              <label>Experience (years)</label>
              <input type="number" min="0" max="60" value={form.experienceYears} onChange={set('experienceYears')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Date of birth</label>
              <input type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} />
            </div>
            <div className="form-group">
              <label>Joining date</label>
              <input type="date" value={form.joiningDate} onChange={set('joiningDate')} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Gender</label>
              <select value={form.gender} onChange={set('gender')}>
                <option value="">—</option>
                <option>MALE</option><option>FEMALE</option><option>OTHER</option>
              </select>
            </div>
            <div className="form-group" />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn" disabled={saving}>
              {saving ? 'Saving…' : editing ? 'Save changes' : 'Add faculty'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
