import { useEffect, useState } from 'react'
import useSeo from './useSeo'
import publicService from '../../services/publicService'

export default function EventsPage() {
  useSeo('Events', 'Upcoming public events at ABC College of Higher Education — fests, open house, convocation and more.')
  const [events, setEvents] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let active = true
    publicService.events()
      .then((d) => active && setEvents(d))
      .catch(() => active && setError(true))
    return () => { active = false }
  }, [])

  return (
    <div className="page">
      <h1>Upcoming Events</h1>
      {error && <p className="alert alert-danger">Could not load events right now — please try again shortly.</p>}
      {events === null && !error && <p className="muted">Loading events…</p>}
      {events !== null && events.length === 0 && !error && (
        <p className="muted">No upcoming events published — check back soon.</p>
      )}
      <div className="event-grid">
        {(events || []).map((e) => (
          <article key={e.id} className="event-card">
            <div className="event-date" aria-hidden="true">
              <span className="event-day">{new Date(e.eventDate).getDate()}</span>
              <span className="event-month">{new Date(e.eventDate).toLocaleString('en', { month: 'short' })}</span>
            </div>
            <div>
              <h3>{e.title}</h3>
              {e.venue && <p className="muted small">📍 {e.venue}</p>}
              {e.description && <p className="small">{e.description}</p>}
            </div>
          </article>
        ))}
      </div>
    </div>
  )
}
