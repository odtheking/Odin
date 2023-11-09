var currentPage = window.location.href.split('/').pop();
var legitElement = document.getElementById('legit');
var cheaterElement = document.getElementById('cheater');


document.addEventListener("DOMContentLoaded", function () {
    var currentPage = window.location.href.split('/').pop();
    var legitElement = document.getElementById('legit');
    var cheaterElement = document.getElementById('cheater');
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

function parseReadmeContent(content) {
    const lines = content.split('\n');
    const modulePairs = [];
    let currentCategory = ''; // Store the current category
    let isCheaterCategory = false; // Initialize as false
    let currentMainCategory = ''; // Store the current category


    for (const line of lines) {
        if (line.startsWith('Category')) {
            // Extract the category name and store it
            if (currentCategory == 'Skyblock' && line == 'Category: Dungeons') {
                isCheaterCategory = true;
            }
            currentCategory = line.replace('Category: ', '');
        } else if (line.startsWith('- ')) {
            // Extract the module name and description
            const match = /^- (.+?): (.+)$/.exec(line);
            if (match) {
                const moduleName = match[1];
                const moduleDescription = match[2];
                // Create an object with category, module name, and module description
                if (isCheaterCategory) {
                    currentMainCategory = "cheater"
                } else {
                    currentMainCategory = "legit"
                }
                const module = {
                    currentMainCategory: currentMainCategory,
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

function populateModuleList(moduleListId, type) {
    const moduleList = document.getElementById(moduleListId); 
    let currentCategory = ''; // Store the current category
    let currentCat
    // Fetch the README content from the GitHub API and handle it in the promise chain
    fetch("https://api.github.com/repos/odtheking/odinclient/contents/README.md")
        .then(response => response.json())
        .then(data => {
            // The content is base64 encoded, so you need to decode it
            const decodedContent = atob(data.content);

            // Parse the README content and get module pairs
            const modulePairs = parseReadmeContent(decodedContent);

            // Iterate over the module pairs and create the module list
            modulePairs.forEach((module, index) => {
                
                if (module.currentMainCategory !== type) return;
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
    if (currentPage === "feature_list_cheater") populateModuleList("module-list-cheater", "cheater") 

    if (currentPage === "feature_list_legit") populateModuleList("module-list-legit", "legit") 

});


function submitForm() {
    // Get the values from the input fields
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;

    // Create an object with the data
    var data = {
        username: username,
        password: password
    };

    // Convert the data to JSON
    var jsonData = JSON.stringify(data);

   
}


