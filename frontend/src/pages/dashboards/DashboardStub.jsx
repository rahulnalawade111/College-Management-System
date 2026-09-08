import { useAuth } from '../../context/AuthContext'

export default function DashboardStub({ area }) {
  const { user } = useAuth()
  return (
    <div className="page">
      <h1>Welcome, {user?.username} 👋</h1>
      <p className="muted">
        You're signed in as <strong>{user?.role}</strong>. The {area} dashboard is being built in the next phase —
        authentication, routing and role-based access are fully wired.
      </p>
      <div className="card">
        <h3>What works right now</h3>
        <ul>
          <li>JWT login against the Spring Boot API</li>
          <li>Backend-enforced role rules (this page is unreachable with the wrong role)</li>
          <li>Protected routes with automatic 401 → login redirect</li>
        </ul>
      </div>
    </div>
  )
}
