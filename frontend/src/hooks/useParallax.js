import { useEffect } from 'react'

// The gentle "moves with the cursor" effect from the original intro page.
// Every element inside `containerRef` with a data-depth attribute drifts
// with the mouse; larger numbers move further.
export function useParallax(containerRef) {
  useEffect(() => {
    const container = containerRef.current
    if (!container) return undefined
    if (
      window.matchMedia('(prefers-reduced-motion: reduce)').matches ||
      window.matchMedia('(hover: none)').matches
    ) {
      return undefined
    }

    const items = [...container.querySelectorAll('[data-depth]')]
    let tx = 0
    let ty = 0
    let x = 0
    let y = 0
    let frame

    const onMove = (event) => {
      tx = event.clientX / window.innerWidth - 0.5
      ty = event.clientY / window.innerHeight - 0.5
    }
    const loop = () => {
      x += (tx - x) * 0.07
      y += (ty - y) * 0.07
      items.forEach((el) => {
        const depth = Number(el.dataset.depth)
        el.style.transform = `translate3d(${x * depth}px, ${y * depth}px, 0)`
      })
      frame = requestAnimationFrame(loop)
    }

    window.addEventListener('mousemove', onMove)
    frame = requestAnimationFrame(loop)
    return () => {
      window.removeEventListener('mousemove', onMove)
      cancelAnimationFrame(frame)
    }
  }, [containerRef])
}
