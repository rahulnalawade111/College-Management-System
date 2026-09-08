import { useEffect, useState, useCallback } from 'react'
import { departmentService } from '../../services/masterDataService'
import { useToast } from '../../context/ToastContext'
import DataTable from '../../components/DataTable'
import Modal from '../../components/Modal'
import { pickErrorMessage } from '../../components/States'

const EMPTY_FORM = { departmentCode: '', departmentName: '', description: '' }

export default function DepartmentsPage() {
  const toast = useToast()
  const [rows, setRows] = useState(null)
  const [error, setError] = useState(null)
  const [modal, setModal] = useState(null) // { mode: 'create'|'edit', row? }
  const [form, setForm] = useState(EMPTY_FORM)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setError(null)
    try {
      setRows(await departmentService.list())
    } catch (err) {
      setError(pickErrorMessage(err, 'Failed to load departments'))
    }
  }, [])

  useEffect(() => { load() }, [load])

  function openCreate() {
    setForm(EMPTY_FORM)
    setModal({ mode: 'create' })
  }

  function openEdit(row) {
    setForm({ departmentCode: row.departmentCode, departmentName: row.departmentName, description: row.description || '' })
    setModal({ mode: 'edit', row })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      if (modal.mode === 'create') {
        await departmentService.create(form)
        toast.success('Department created')
      } else {
        await departmentService.update(modal.row.id, form)
        toast.success('Department updated')
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
    if (!window.confirm(`Delete department "${row.departmentName}"?`)) return
    try {
      await departmentService.remove(row.id)
      toast.success('Department deleted')
      await load()
    } catch (err) {
      toast.error(pickErrorMessage(err, 'Delete failed'))
    }
  }

  const columns = [
    { key: 'departmentCode', label: 'Code', sortable: true },
    { key: 'departmentName', label: 'Name', sortable: true },
    { key: 'description', label: 'Description', render: (r) => <span className="muted">{r.description || '—'}</span> },
    {
      key: 'actions',
      label: 'Actions',
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
          <h1>Departments</h1>
          <p className="muted">{rows?.length ?? '…'} departments</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={openCreate}>+ Add Department</button>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          rows={rows || []}
          loading={rows === null && !error}
          error={error}
          onRetry={load}
          emptyMessage="No departments found."
        />
      </div>

      {modal && (
        <Modal title={modal.mode === 'create' ? 'Add Department' : 'Edit Department'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label>Code</label>
              <input value={form.departmentCode} required maxLength={20}
                     onChange={(e) => setForm({ ...form, departmentCode: e.target.value.toUpperCase() })}
                     placeholder="e.g. AI" />
            </div>
            <div className="form-field">
              <label>Name</label>
              <input value={form.departmentName} required maxLength={120}
                     onChange={(e) => setForm({ ...form, departmentName: e.target.value })}
                     placeholder="e.g. Artificial Intelligence" />
            </div>
            <div className="form-field">
              <label>Description</label>
              <textarea rows={3} value={form.description} maxLength={500}
                        onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setModal(null)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={busy}>
                {busy ? 'Saving…' : 'Save'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
