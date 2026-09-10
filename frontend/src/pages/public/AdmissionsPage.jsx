import { useState } from 'react'
import { Link } from 'react-router-dom'
import useSeo from './useSeo'
import publicService from '../../services/publicService'

export default function AdmissionsPage() {
  useSeo('Admissions', 'Admission process, eligibility, important dates and scholarships at ABC College of Higher Education.')
  const [form, setForm] = useState({ name: '', email: '', phone: '', message: '' })
  const [status, setStatus] = useState(null) // null | 'sending' | 'sent' | 'error'

  const submit = async (e) => {
    e.preventDefault()
    setStatus('sending')
    try {
      await publicService.submitContact({
        ...form,
        message: `[ADMISSIONS ENQUIRY] ${form.message}`,
      })
      setStatus('sent')
      setForm({ name: '', email: '', phone: '', message: '' })
    } catch {
      setStatus('error')
    }
  }

  return (
    <div className="page">
      <h1>Admissions 2026–27</h1>
      <p>Applications for all programs are now open. Merit scholarships cover up to 100% tuition for top-ranked applicants.</p>

      <h2>Process</h2>
      <ol>
        <li>Submit the enquiry form below or apply through the portal.</li>
        <li>Appear for the entrance assessment (program-specific).</li>
        <li>Attend counselling and document verification.</li>
        <li>Confirm admission and pay the first-semester fee.</li>
      </ol>

      <h2>Important Dates</h2>
      <ul>
        <li>Application deadline — 30 June 2026</li>
        <li>Entrance assessment — 12–18 July 2026</li>
        <li>Counselling & admission confirmation — 20–31 July 2026</li>
        <li>Orientation — first week of August 2026</li>
      </ul>

      <h2>Enquiry Form</h2>
      {status === 'sent' && <p className="alert alert-success">Thanks! Our admissions team will contact you shortly.</p>}
      {status === 'error' && <p className="alert alert-danger">Something went wrong — please retry or email admissions@abccollege.edu.</p>}
      <form onSubmit={submit} className="contact-form" noValidate={false}>
        <label>Name
          <input required maxLength={120} value={form.name}
                 onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </label>
        <label>Email
          <input required type="email" maxLength={180} value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <label>Phone (optional)
          <input maxLength={30} value={form.phone}
                 onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </label>
        <label>Your question
          <textarea required maxLength={5000} rows={4} value={form.message}
                    onChange={(e) => setForm({ ...form, message: e.target.value })} />
        </label>
        <button className="btn btn-primary" disabled={status === 'sending'} type="submit">
          {status === 'sending' ? 'Sending…' : 'Submit enquiry'}
        </button>
      </form>

      <p className="muted small">Already a student? <Link to="/login">Sign in to the portal</Link>.</p>
    </div>
  )
}
