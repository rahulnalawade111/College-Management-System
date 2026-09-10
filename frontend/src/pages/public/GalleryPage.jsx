import useSeo from './useSeo'

const PHOTOS = [
  { src: '/assets/gallery/campus.svg', alt: 'Main campus building with green lawns' },
  { src: '/assets/gallery/library.svg', alt: 'Students studying in the central library' },
  { src: '/assets/gallery/lab.svg', alt: 'Students working in an engineering laboratory' },
  { src: '/assets/gallery/sports.svg', alt: 'Inter-college sports meet on the athletics track' },
  { src: '/assets/gallery/graduation.svg', alt: 'Graduation day ceremony at the central lawn' },
  { src: '/assets/gallery/fest.svg', alt: 'Students performing at the annual cultural fest' },
  { src: '/assets/gallery/hostel.svg', alt: 'Student hostel residential block' },
  { src: '/assets/gallery/cafeteria.svg', alt: 'Campus cafeteria food court' },
]

export default function GalleryPage() {
  useSeo('Gallery', 'Photo gallery of campus life at ABC College of Higher Education.')
  return (
    <div className="page">
      <h1>Campus Gallery</h1>
      <p className="muted">A glimpse of life at ABC College — academics, sports, fests and more.</p>
      <div className="gallery-grid">
        {PHOTOS.map((p) => (
          <img key={p.src} src={p.src} alt={p.alt} loading="lazy" width="320" height="200"
               onError={(ev) => { ev.currentTarget.src = '/assets/gallery/fallback.svg' }} />
        ))}
      </div>
    </div>
  )
}
