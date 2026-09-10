import { useEffect } from 'react'

/** Sets document title + meta description for a public page. */
export default function useSeo(title, description) {
  useEffect(() => {
    if (title) document.title = `${title} — ABC College of Higher Education`
    if (description) {
      let meta = document.querySelector('meta[name="description"]')
      if (!meta) {
        meta = document.createElement('meta')
        meta.setAttribute('name', 'description')
        document.head.appendChild(meta)
      }
      meta.setAttribute('content', description)
    }
  }, [title, description])
}
