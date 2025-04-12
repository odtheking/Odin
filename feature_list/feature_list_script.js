document.addEventListener("DOMContentLoaded", () => {
    const legitElement = document.getElementById('legit')
    const cheaterElement = document.getElementById('cheater')
    const moduleContainer = document.getElementById('module-container')

    if (legitElement) legitElement.addEventListener('click', () => setActiveView('legit'))
    if (cheaterElement) cheaterElement.addEventListener('click', () => setActiveView('cheat'))

    loadAllModules()

    function setActiveView(view) {
        if (legitElement) legitElement.style.textDecoration = view === 'legit' ? 'underline' : 'none'
        if (cheaterElement) cheaterElement.style.textDecoration = view === 'cheat' ? 'underline' : 'none'

        document.querySelectorAll('.module-category').forEach(category => {
            const isLegit = category.getAttribute('data-type') === 'legit'
            category.style.display = (view === 'legit' && isLegit) || (view === 'cheat' && !isLegit) ? 'block' : 'none'
        })
    }

    function loadAllModules() {
        fetch("https://raw.githubusercontent.com/odtheking/Odin/main/FEATURELISTS.md")
            .then(response => {
                if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`)
                return response.text()
            })
            .then(data => {
                const modulePairs = parseReadmeContent(data)
                const groupedModules = {}

                modulePairs.forEach(module => {
                    const type = module.isLegitCategory ? 'legit' : 'cheat'
                    if (!groupedModules[type]) groupedModules[type] = {}
                    if (!groupedModules[type][module.category]) groupedModules[type][module.category] = []
                    groupedModules[type][module.category].push(module)
                })

                for (const type in groupedModules) {
                    for (const categoryName in groupedModules[type]) {
                        const categoryContainer = document.createElement('div')
                        categoryContainer.classList.add('module-category')
                        categoryContainer.setAttribute('data-type', type)

                        const categoryHeader = document.createElement('div')
                        categoryHeader.classList.add('category-header')
                        categoryHeader.textContent = categoryName
                        categoryContainer.appendChild(categoryHeader)

                        groupedModules[type][categoryName].forEach(module => {
                            const moduleItem = document.createElement('div')
                            moduleItem.classList.add('module-item')

                            const moduleName = document.createElement('div')
                            moduleName.classList.add('module-name')
                            moduleName.textContent = module.name

                            const moduleDescription = document.createElement('div')
                            moduleDescription.classList.add('module-description')
                            moduleDescription.textContent = module.description
                            moduleDescription.style.display = 'none'

                            moduleName.addEventListener('click', () => {
                                moduleDescription.style.display =
                                    moduleDescription.style.display === 'block' ? 'none' : 'block'
                            })

                            moduleItem.appendChild(moduleName)
                            moduleItem.appendChild(moduleDescription)
                            categoryContainer.appendChild(moduleItem)
                        })

                        moduleContainer.appendChild(categoryContainer)
                    }
                }

                setActiveView('legit')
            })
            .catch(error => console.error('Error fetching or processing data:', error))
    }
})

const parseReadmeContent = content => {
    const lines = content.split('\n')
    let isLegitCategory = true
    let currentCategory = ''
    const modulePairs = []

    for (const line of lines) {
        if (line.startsWith('Category')) {
            if (currentCategory === 'Nether' && line === 'Category: Dungeon') isLegitCategory = false
            currentCategory = line.replace('Category: ', '')
        } else if (line.startsWith('- ')) {
            const match = /^- (.+?): (.+)$/.exec(line)
            if (match) {
                const [, moduleName, moduleDescription] = match
                modulePairs.push({
                    isLegitCategory,
                    category: currentCategory,
                    name: moduleName,
                    description: moduleDescription
                })
            }
        }
    }
    return modulePairs
}