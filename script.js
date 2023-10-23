var currentPage = window.location.href.split('/').pop();

document.addEventListener("DOMContentLoaded", function () {
    var currentPage = window.location.href.split('/').pop();
    var legitElement = document.getElementById('legit');
    var cheaterElement = document.getElementById('cheater');

    if (legitElement) {
        legitElement.addEventListener('click', function() {
            if (currentPage === 'cheater.html' || currentPage === 'legit.html') {
                window.location.href = 'legit.html'; // Redirect to legit.html
            } else if (currentPage === 'feature_list_legit.html' || currentPage === 'feature_list_cheater.html') {
                window.location.href = 'feature_list_legit.html'; // Redirect to feature_list_legit.html
            }
        });
    }

    if (cheaterElement) {
        cheaterElement.addEventListener('click', function() {
            if (currentPage === 'cheater.html' || currentPage === 'legit.html') {
                window.location.href = 'cheater.html'; // Redirect to cheater.html
            } else if (currentPage === 'feature_list_legit.html' || currentPage === 'feature_list_cheater.html') {
                window.location.href = 'feature_list_cheater.html'; // Redirect to feature_list_cheater.html
            }
        });
    }
});


var currentPage = window.location.href.split('/').pop();
if (currentPage === 'legit.html') {
    document.getElementById('legit').style.textDecoration = 'underline';
} else if (currentPage === 'cheater.html') {
    document.getElementById('cheater').style.textDecoration = 'underline';
}

function onClick() {
    
    fetch('https://api.thecatapi.com/v1/images/search')
        .then(response => response.json())
        .then(data => {
            // Assuming data is an array with a single object
            if (Array.isArray(data) && data.length > 0) {
            const imageUrl = data[0].url;

            var image = document.getElementById('cat');
            image.src = imageUrl;



            // Now you can use the imageUrl variable in your code as needed.
            } else {
            console.error('No cat images found in the response');
            }
        }).catch(error => console.error('Error:', error)
    );
    
}

window.onload = function() {
    onClick(); // Call the function when the page is fully loaded
}


// JavaScript
const apiUrlCheater = `https://api.github.com/repos/odtheking/odinclient/contents/README.md`;
const apiUrlLegit = `https://api.github.com/repos/odtheking/odin/contents/README.md`;

function parseReadmeContent(content) {
    const lines = content.split('\n');
    const modulePairs = [];
    let currentCategory = ''; // Store the current category

    for (const line of lines) {
        if (line.startsWith('Category')) {
            // Extract the category name and store it
            currentCategory = line.replace('Category: ', '');
        } else if (line.startsWith('- ')) {
            // Extract the module name and description
            const match = /^- (.+?): (.+)$/.exec(line);
            if (match) {
                const moduleName = match[1];
                const moduleDescription = match[2];
                // Create an object with category, module name, and module description
                const module = {
                    category: currentCategory,
                    name: moduleName,
                    description: moduleDescription
                };
                modulePairs.push(module);
            }
        }
    }

    return modulePairs;
}

function populateModuleList(apiUrl, moduleListId) {
    const moduleList = document.getElementById(moduleListId);
    let currentCategory = ''; // Store the current category
    let isLegitCategory = true; // Initialize as true

    // Fetch the README content from the GitHub API and handle it in the promise chain
    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            // The content is base64 encoded, so you need to decode it
            const decodedContent = atob(data.content);

            // Parse the README content and get module pairs
            const modulePairs = parseReadmeContent(decodedContent);

            // Log the module pairs (or use them as needed)
            //console.log(modulePairs);

            // Filter and display only the "LEGIT" or "CHEATER" features
            modulePairs.forEach((module, index) => {
                // Check for "LEGIT" and "CHEATER" keywords
                if (module.name.includes("LEGIT")) {
                    isLegitCategory = true;
                    return; // Skip to the next module
                } else if (module.name.includes("CHEATER")) {
                    isLegitCategory = false;
                    return; // Skip to the next module
                }

                // Only display features in the "LEGIT" or "CHEATER" category
                if (isLegitCategory) {
                    // Check if the category has changed
                    if (module.category !== currentCategory) {
                        // Create a blank row before the new category
                        const blankRow = document.createElement("div");
                        blankRow.classList.add("blank-row");
                        moduleList.appendChild(blankRow);

                        // Create a category header without "Category:" prefix
                        const categoryHeader = document.createElement("div");
                        categoryHeader.classList.add("category-header");
                        categoryHeader.textContent = module.category.replace('Category: ', '');
                        moduleList.appendChild(categoryHeader);

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
                    moduleList.appendChild(moduleItem);
                }
            });
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

document.addEventListener("DOMContentLoaded", function () {
    if (currentPage === "feature_list_legit.html") populateModuleList(apiUrlLegit, "module-list-legit");

    // Populate the "CHEATER" list
    if (currentPage === "feature_list_cheater.html") populateModuleList(apiUrlCheater, "module-list-cheater") 
});










/*
https://api.thecatapi.com/v1/images/search
[{"id":"aoc","url":"https://cdn2.thecatapi.com/images/aoc.jpg","width":628,"height":956}]


*/