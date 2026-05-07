function initTitleSearch(itemClass, inputId, listId) {
  const minFuzzyScore = 0.5
  const itemBlocks = document.getElementsByClassName(itemClass)
  const input = document.getElementById(inputId)
  const titles = []
  const idByTitle = {}
  let searchSet

  for (let item of itemBlocks) {
    const title = item.getAttribute('data-title').normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    const id = item.getAttribute('data-id')
    titles.push(title)
    idByTitle[title] = id
  }
  searchSet = FuzzySet(titles, false, 2, 3)

  input.addEventListener('keyup', () => {
    const value = input.value.trim()
          .normalize("NFD").replace(/[\u0300-\u036f]/g, "")

    const isSearchMode = value.length > 2
    toggleSearchMode(isSearchMode)
    if (isSearchMode) {
      const lowerValue = value.toLowerCase()
      const substringMatches = titles.filter(t => t.toLowerCase().includes(lowerValue))
      if (substringMatches.length > 0) {
        displayAnswers(substringMatches)
      } else {
        const a = searchSet.get(value, null, minFuzzyScore)
        if (a != null) {
          displayAnswers(a.map(v => v[1]))
        }
      }
    }
  })

  function displayAnswers(titleAnswers) {
    const answers = document.querySelector('#answers')
    while (answers.children[0]) {
      answers.removeChild(answers.children[0])
    }
    for (let title of titleAnswers) {
      const id = idByTitle[title]
      const m = document.getElementById(id).cloneNode(true)
      delete m.id
      let lazyImages = m.querySelectorAll('[loading=lazy]')
      lazyImages.forEach(image => {
        image.src = image.dataset.src
      })
      answers.appendChild(m)
    }
  }

  function toggleSearchMode(actived) {
    const answers = document.querySelector('#answers')
    const list = document.querySelector(listId)

    if (actived) {
      answers.style.removeProperty('display')
      list.style.display = 'none'
    } else {
      answers.style.display = 'none'
      list.style.removeProperty('display')
    }
  }
}
