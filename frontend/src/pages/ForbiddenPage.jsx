import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <div className="auth-page">
      <div className="auth-card center">
        <h1>403</h1>
        <p>You don't have permission to view that page.</p>
        <Link className="btn btn-primary" to="/">Back to home</Link>
      </div>
    </div>
  )
}
