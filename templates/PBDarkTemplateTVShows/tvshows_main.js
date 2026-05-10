window.addEventListener('load', () => {

  // lazy loading of images

  let images = [...document.querySelectorAll('[loading=lazy]')]

  const interactSettings = {
    root: null,
    rootMargin: '0px 0px 200px 0px'
  }

  function onIntersection(imageEntites) {
    imageEntites.forEach(image => {
      if (image.isIntersecting) {
        observer.unobserve(image.target)
        image.target.src = image.target.dataset.src
      }
    })
  }

  let observer = new IntersectionObserver(onIntersection, interactSettings)

  images.forEach(image => observer.observe(image))

  // title search
  initTitleSearch('media-card', 'showtitle', '#shows')

})
