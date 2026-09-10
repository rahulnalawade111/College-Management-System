import { useState } from 'react'
import useSeo from './useSeo'
import publicService from '../../services/publicService'

export default function ContactPage() {
  useSeo('Contact Us', 'Contact ABC College of Higher Education — address, phone, email and enquiry form.')
  const [form, setForm] = useState({ name: '', email: '', phone: '', message: '' })
  const [status, setStatus] = useState(null)

  const submit = async (e) => {
    e.preventDefault()
    setStatus('sending')
    try {
      await publicService.submitContact(form)
      setStatus('sent')
      setForm({ name: '', email: '', phone: '', message: '' })
    } catch {
      setStatus('error')
    }
  }

  return (
    <div className="page">
      <h1>Contact Us</h1>
      <div className="contact-columns">
        <div>
          <h2>Reach us</h2>
          <p>
            ABC College of Higher Education<br />
            123 University Road, Knowledge City 400001<br />
            Phone: +91 12345 67890<br />
            Email: info@abccollege.edu<br />
            Admissions: admissions@abccollege.edu
          </p>
          <p className="muted small">Office hours: Mon–Sat, 9:00 AM – 5:00 PM</p>
        </div>
        <div>
          <h2>Send a message</h2>
          {status === 'sent' && <p className="alert alert-success">Thanks! We'll get back to you shortly.</p>}
          {status === 'error' && <p className="alert alert-danger">Something went wrong — please retry or email us directly.</p>}
          <form onSubmit={submit} className="contact-form">
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
            <label>Message
              <textarea required maxLength={5000} rows={4} value={form.message}
                        onChange={(e) => setForm({ ...form, message: e.target.value })} />
            </label>
            <button className="btn btn-primary" disabled={status === 'sending'} type="submit">
              {status === 'sending' ? 'Sending…' : 'Send message'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
