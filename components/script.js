document.addEventListener("DOMContentLoaded", () => {
})

const loadNavbar = () => {
    const navbarPlaceholder = document.getElementById('navbar-placeholder')

    fetch('components/navbar.html')
        .then(response => response.text())
        .then(data => {
            navbarPlaceholder.innerHTML = data

            adjustNavbarPaths()
        }).catch(error => console.error('Error loading navbar:', error))
}

const adjustNavbarPaths = () => {
    const isSubdirectory = window.location.pathname.split('/').length > 2

    if (isSubdirectory) {
        document.querySelectorAll('#navbar-placeholder img').forEach(img => {
            if (img.src.startsWith('http')) return // Skip absolute URLs
            img.src = img.src.replace('/assets', '../assets')
        })
    }
}

const loadFooter = () => {
    const footerPlaceholder = document.getElementById('footer-placeholder')

    fetch('components/footer.html')
        .then(response => response.text())
        .then(data => {
            footerPlaceholder.innerHTML = data

            adjustFooterPaths()

            const catElement = document.getElementById('cat')
            if (catElement) {
                catElement.addEventListener("click", () => {
                    console.log("Cat image clicked")
                    loadCatImage()
                })
            }
            loadCatImage()
        }).catch(error => console.error('Error loading footer:', error))
}

const adjustFooterPaths = () => {
    const isSubdirectory = window.location.pathname.split('/').length > 2

    if (isSubdirectory) {
        document.querySelectorAll('#footer-placeholder a').forEach(link => {
            if (link.href.startsWith('http')) return // Skip absolute URLs
            link.href = '../' + link.getAttribute('href')
        })
    }
}

const loadCatImage = () => {
    const catElement = document.getElementById('cat')
    if (!catElement) return console.warn("Cat image element not found")

    catElement.addEventListener("click", () => {
        console.log("Cat image clicked")
        loadCatImage()
    })

    fetch('https://api.thecatapi.com/v1/images/search')
        .then(response => response.json())
        .then(data => {
            if (Array.isArray(data) && data.length > 0) catElement.src = data[0].url
            else console.error('No cat images found in the response')
        }).catch(error => console.error('Error loading cat image:', error.toString()))
}