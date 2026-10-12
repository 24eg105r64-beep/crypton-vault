(() => {
  if (matchMedia('(prefers-reduced-motion: reduce)').matches || matchMedia('(hover: none)').matches) return;
  const items = [...document.querySelectorAll('[data-depth]')];
  let tx = 0, ty = 0, x = 0, y = 0;
  addEventListener('mousemove', e => { tx = e.clientX / innerWidth - .5; ty = e.clientY / innerHeight - .5; });
  (function loop() {
    x += (tx - x) * .07; y += (ty - y) * .07;
    items.forEach(el => { const d = +el.dataset.depth; el.style.transform = `translate3d(${x * d}px, ${y * d}px, 0)`; });
    requestAnimationFrame(loop);
  })();
})();
