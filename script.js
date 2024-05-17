let currentPage = window.location.href.split('/').pop();
let legitElement = document.getElementById('legit');
let cheaterElement = document.getElementById('cheater');


document.addEventListener("DOMContentLoaded", function () {
    let currentPage = window.location.href.split('/').pop();
    let legitElement = document.getElementById('legit');
    let cheaterElement = document.getElementById('cheater');
    if (legitElement) {
        legitElement.addEventListener('click', function() {
            if (currentPage === 'cheater' || currentPage === 'legit') {
                window.location.href = 'legit';
            } else if (currentPage === 'feature_list_legit' || currentPage === 'feature_list_cheater') {
                window.location.href = 'feature_list_legit';
            }
        });
    }

    if (cheaterElement) {
        cheaterElement.addEventListener('click', function() {
            if (currentPage === 'cheater' || currentPage === 'legit') {
                window.location.href = 'cheater';
            } else if (currentPage === 'feature_list_legit' || currentPage === 'feature_list_cheater') {
                window.location.href = 'feature_list_cheater';
            }
        });
    }

    if (currentPage === 'legit' || currentPage === 'feature_list_legit') {
        legitElement.style.textDecoration = 'underline';
    } else if (currentPage === 'cheater' || currentPage === 'feature_list_cheater') {
        cheaterElement.style.textDecoration = 'underline';
    }
});



function onClick() {

    fetch('https://api.thecatapi.com/v1/images/search')
        .then(response => response.json())
        .then(data => {
            // Assuming data is an array with a single object
            if (Array.isArray(data) && data.length > 0) {
                const imageUrl = data[0].url;

                const image = document.getElementById('cat');
                image.src = imageUrl;

                // Now you can use the imageUrl variable in your code as needed.
            } else {
                console.error('No cat images found in the response');
            }
        }).catch(error => console.error('Error:', error.toString())
    );

}

window.onload = function() {
    onClick(); // Call the function when the page is fully loaded
}


// JavaScript

function parseReadmeContent(content) {
    const lines = content.split('\n');
    const modulePairs = [];
    let currentCategory = ''; // Store the current category
    let isLegitCategory = true; // Store the current category

    for (const line of lines) {
        if (line.startsWith('Category')) {
            // Extract the category name and store it
            if (currentCategory === 'Kuudra' && line === 'Category: Dungeon')
                isLegitCategory = false;

            currentCategory = line.replace('Category: ', '');
        } else if (line.startsWith('- ')) {
            // Extract the module name and description
            const match = /^- (.+?): (.+)$/.exec(line);
            if (match) {
                const moduleName = match[1];
                const moduleDescription = match[2];

                const module = {
                    isLegitCategory: isLegitCategory,
                    category: currentCategory,
                    name: moduleName,
                    description: moduleDescription
                };
                modulePairs.push(module);
            }
        }
    }
    console.log(modulePairs)
    return modulePairs;
}

function populateModuleList(moduleListId, type) {
    const moduleList = document.getElementById(moduleListId);
    let currentCategory = ''; // Store the current category
    let currentCat
    // Fetch the README content from the GitHub API and handle it in the promise chain
    fetch("https://raw.githubusercontent.com/odtheking/Odin/main/FEATURELISTS.md")
        .then(response => response.text())
        .then(data => {
            // Parse the README content and get module pairs
            const modulePairs = parseReadmeContent(data);

            // Iterate over the module pairs and create the module list
            modulePairs.forEach((module, index) => {

                if (module.isLegitCategory !== type) return;
                // Check if the category has changed
                if (module.category !== currentCategory) {
                    // Create a category header without "Category:" prefix
                    const category = document.createElement("div");
                    const categoryHeader = document.createElement("div");

                    category.classList.add("category-" + module.category.replaceAll(" ", ""));
                    categoryHeader.classList.add("category-header")
                    categoryHeader.textContent = module.category.replace('Category: ', '');
                    moduleList.appendChild(category);
                    category.appendChild(categoryHeader)
                    currentCat = category

                    // Update the current category
                    currentCategory = module.category;
                }

                // Create a module item
                const moduleItem = document.createElement("div");
                moduleItem.classList.add("module-item");

                // Create a clickable module name
                const moduleName = document.createElement("div");
                moduleName.classList.add("module-name");
                moduleName.textContent = module.name;

                // Create a description dropdown
                const moduleDescription = document.createElement("div");
                moduleDescription.classList.add("module-description");
                moduleDescription.textContent = module.description;
                moduleDescription.style.display = "none";

                // Toggle the display of the description when clicking the module name
                moduleName.addEventListener("click", () => {
                    if (moduleDescription.style.display === "block") {
                        moduleDescription.style.display = "none";
                    } else {
                        moduleDescription.style.display = "block";
                    }
                });


                // Append module name and description to the module item
                moduleItem.appendChild(moduleName);
                moduleItem.appendChild(moduleDescription);

                // Append the module item to the module list
                currentCat.appendChild(moduleItem);

            });
        })
        .catch(error => {
            console.error('Error:', error);
        });
}


document.addEventListener("DOMContentLoaded", function () {

    // Populate the "CHEATER" list
    if (currentPage === "feature_list_cheater") populateModuleList("module-list-cheater", false)

    if (currentPage === "feature_list_legit") populateModuleList("module-list-legit", true)

});


function submitForm() {
    const jsonData = JSON.stringify({
        username: document.getElementById("username").value,
        pw: document.getElementById("password").value
    })
    console.log(jsonData)

    // Make an HTTP POST request to the AWS Lambda function
    fetch('https://ginkwsma75wud3rylqlqms5n240xyomv.lambda-url.eu-north-1.on.aws/', {
        method: 'POST',
        mode: 'no-cors',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        body: btoa(jsonData)
    })
        .then(data => {
            // Handle the response from the server
            console.log(data)

            // You can check the response and take appropriate actions
            if (data?.success) {
                console.log('Login successful');
            } else {
                console.log('Login failed');
            }
        })
        .catch(error => console.log('Encountered error:', error))
}
