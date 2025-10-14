document.addEventListener('DOMContentLoaded', () => {
    const legitButton = document.getElementById('legit');
    const cheaterButton = document.getElementById('cheater');
    const legitContent = document.getElementById('legit-content');
    const cheaterContent = document.getElementById('cheater-content');

    function switchVersion(isLegit) {
        if (isLegit) {
            legitButton.classList.add('active');
            cheaterButton.classList.remove('active');
            legitContent.classList.add('active');
            cheaterContent.classList.remove('active');
            document.title = "Odin Download - Official Website";
        } else {
            cheaterButton.classList.add('active');
            legitButton.classList.remove('active');
            cheaterContent.classList.add('active');
            legitContent.classList.remove('active');
            document.title = "OdinClient Download - Official Website";
        }
    }

    legitButton.addEventListener('click', () => switchVersion(true));
    cheaterButton.addEventListener('click', () => switchVersion(false));

    const yearElement = document.getElementById('year');
    if (yearElement) {
        yearElement.textContent = new Date().getFullYear();
    }

    const catElement = document.getElementById('cat');
    if (catElement) {
        function loadCatImage() {
            fetch('https://api.thecatapi.com/v1/images/search')
                .then(response => response.json())
                .then(data => {
                    if (Array.isArray(data) && data.length > 0) catElement.src = data[0].url;
                })
                .catch(error => console.error('Error loading cat image:', error));
        }

        catElement.addEventListener('click', loadCatImage);
        loadCatImage();
    }
});
