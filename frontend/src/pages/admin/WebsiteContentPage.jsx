import { useCallback, useEffect, useState } from 'react'
import api from '../../services/api'
import DataTable from '../../components/DataTable'
import Modal from '../../components/Modal'
import { Loading } from '../../components/States'
import { useToast } from '../../context/ToastContext'

const emptyNotice = { title: '', body: '', status: 'DRAFT' }
const emptyEvent = { title: '', description: '', eventDate: '', venue: '', status: 'DRAFT' }

export default function WebsiteContentPage() {
  const toast = useToast()
  const [tab, setTab] = useState('notices') // notices | events | messages
  const [notices, setNotices] = useState(null)
  const [events, setEvents] = useState(null)
  const [messages, setMessages] = useState(null)
  const [error, setError] = useState(false)

  const [noticeModal, setNoticeModal] = useState(null) // {mode:'create'|'edit', data}
  const [eventModal, setEventModal] = useState(null)

  const load = useCallback(async () => {
    setError(false)
    try {
      const [n, e, m] = await Promise.all([
        api.get('/admin/notices').then((r) => r.data),
        api.get('/admin/events').then((r) => r.data),
        api.get('/contact').then((r) => r.data),
      ])
      setNotices(n); setEvents(e); setMessages(m)
    } catch {
      setError(true)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const saveNotice = async () => {
    const { mode, data } = noticeModal
    try {
      if (mode === 'create') await api.post('/admin/notices', data)
      else await api.put(`/admin/notices/${data.id}`, data)
      toast('Notice saved')
      setNoticeModal(null); load()
    } catch (err) {
      toast(err.response?.data?.message || 'Save failed', 'error')
    }
  }

  const saveEvent = async () => {
    const { mode, data } = eventModal
    try {
      if (mode === 'create') await api.post('/admin/events', data)
      else await api.put(`/admin/events/${data.id}`, data)
      toast('Event saved')
      setEventModal(null); load()
    } catch (err) {
      toast(err.response?.data?.message || 'Save failed', 'error')
    }
  }

  if (error) return <ErrorState onRetry={load} />
  if (notices === null || events === null || messages === null) return <Loading />

  return (
    <div>
      <div className="section-card-head">
        <h1>Website Content</h1>
      </div>
      <p className="muted small">Notices and events published here appear instantly on the public website.</p>

      <div className="tab-row" role="tablist">
        {[['notices', 'Notices'], ['events', 'Events'], ['messages', `Messages (${messages.length})`]].map(([k, label]) => (
          <button key={k} role="tab" aria-selected={tab === k}
                  className={`tab ${tab === k ? 'active' : ''}`}
                  onClick={() => setTab(k)}>{label}</button>
        ))}
      </div>

      {tab === 'notices' && (
        <>
          <button className="btn btn-primary" onClick={() => setNoticeModal({ mode: 'create', data: { ...emptyNotice } })}>
            + New Notice
          </button>
          <DataTable
            columns={['Title', 'Status', 'Published', 'Actions']}
            rows={notices.map((n) => [
              n.title,
              n.status,
              n.publishedAt ? new Date(n.publishedAt).toLocaleDateString() : '—',
              <span key="a" className="row-actions">
                <button className="btn btn-ghost" onClick={() => setNoticeModal({ mode: 'edit', data: { ...n } })}>Edit</button>
                <button className="btn btn-ghost" onClick={async () => {
                  await api.delete(`/admin/notices/${n.id}`); toast('Notice deleted'); load()
                }}>Delete</button>
              </span>,
            ])}
          />
        </>
      )}

      {tab === 'events' && (
        <>
          <button className="btn btn-primary" onClick={() => setEventModal({ mode: 'create', data: { ...emptyEvent } })}>
            + New Event
          </button>
          <DataTable
            columns={['Title', 'Date', 'Venue', 'Status', 'Actions']}
            rows={events.map((e) => [
              e.title,
              e.eventDate,
              e.venue || '—',
              e.status,
              <span key="a" className="row-actions">
                <button className="btn btn-ghost" onClick={() => setEventModal({ mode: 'edit', data: { ...e } })}>Edit</button>
                <button className="btn btn-ghost" onClick={async () => {
                  await api.delete(`/admin/events/${e.id}`); toast('Event deleted'); load()
                }}>Delete</button>
              </span>,
            ])}
          />
        </>
      )}

      {tab === 'messages' && (
        messages.length === 0
          ? <p className="muted">No contact messages yet.</p>
          : <div className="message-list">
              {messages.map((m) => (
                <article key={m.id} className={`section-card ${m.status === 'NEW' ? 'message-new' : ''}`}>
                  <div className="section-card-head">
                    <h3>{m.name} <span className="muted small">&lt;{m.email}&gt;</span>{m.phone && <span className="muted small"> · {m.phone}</span>}</h3>
                    <span className="row-actions">
                      {m.status === 'NEW' && (
                        <button className="btn btn-ghost" onClick={async () => {
                          await api.patch(`/contact/${m.id}/read`); load()
                        }}>Mark read</button>
                      )}
                      <button className="btn btn-ghost" onClick={async () => {
                        await api.delete(`/contact/${m.id}`); toast('Message deleted'); load()
                      }}>Delete</button>
                    </span>
                  </div>
                  <p className="small">{m.message}</p>
                  <p className="muted small">{new Date(m.createdAt).toLocaleString()}</p>
                </article>
              ))}
            </div>
      )}

      {noticeModal && (
        <Modal title={noticeModal.mode === 'create' ? 'New Notice' : 'Edit Notice'} onClose={() => setNoticeModal(null)}>
          <div className="form-grid">
            <label>Title
              <input value={noticeModal.data.title} maxLength={200}
                     onChange={(e) => setNoticeModal({ ...noticeModal, data: { ...noticeModal.data, title: e.target.value } })} />
            </label>
            <label>Body
              <textarea rows={4} value={noticeModal.data.body || ''}
                        onChange={(e) => setNoticeModal({ ...noticeModal, data: { ...noticeModal.data, body: e.target.value } })} />
            </label>
            <label>Status
              <select value={noticeModal.data.status}
                      onChange={(e) => setNoticeModal({ ...noticeModal, data: { ...noticeModal.data, status: e.target.value } })}>
                <option value="DRAFT">Draft</option>
                <option value="PUBLISHED">Published</option>
              </select>
            </label>
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => setNoticeModal(null)}>Cancel</button>
            <button className="btn btn-primary" onClick={saveNotice} disabled={!noticeModal.data.title}>Save</button>
          </div>
        </Modal>
      )}

      {eventModal && (
        <Modal title={eventModal.mode === 'create' ? 'New Event' : 'Edit Event'} onClose={() => setEventModal(null)}>
          <div className="form-grid">
            <label>Title
              <input value={eventModal.data.title} maxLength={200}
                     onChange={(e) => setEventModal({ ...eventModal, data: { ...eventModal.data, title: e.target.value } })} />
            </label>
            <label>Date
              <input type="date" value={eventModal.data.eventDate || ''}
                     onChange={(e) => setEventModal({ ...eventModal, data: { ...eventModal.data, eventDate: e.target.value } })} />
            </label>
            <label>Venue
              <input value={eventModal.data.venue || ''} maxLength={200}
                     onChange={(e) => setEventModal({ ...eventModal, data: { ...eventModal.data, venue: e.target.value } })} />
            </label>
            <label>Description
              <textarea rows={3} value={eventModal.data.description || ''}
                        onChange={(e) => setEventModal({ ...eventModal, data: { ...eventModal.data, description: e.target.value } })} />
            </label>
            <label>Status
              <select value={eventModal.data.status}
                      onChange={(e) => setEventModal({ ...eventModal, data: { ...eventModal.data, status: e.target.value } })}>
                <option value="DRAFT">Draft</option>
                <option value="PUBLISHED">Published</option>
              </select>
            </label>
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => setEventModal(null)}>Cancel</button>
            <button className="btn btn-primary" onClick={saveEvent}
                    disabled={!eventModal.data.title || !eventModal.data.eventDate}>Save</button>
          </div>
        </Modal>
      )}
    </div>
  )
}
