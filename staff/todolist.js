// Create a "close" button and append it to each list item

document.addEventListener('DOMContentLoaded', function() {
    // Add close button to existing todo items
    const todoItems = document.getElementsByTagName("li");
    for (const item of todoItems) {
        const span = document.createElement("span");
        const txt = document.createTextNode("\u00D7");
        span.className = "close";
        span.appendChild(txt);
        item.appendChild(span);

        // Add event listener to the close button to remove the todo item when clicked
        span.addEventListener("click", function() {
            this.parentElement.remove()
        });
    }

    const close = document.getElementsByClassName("close");
    for (const element of close) {
        element.onclick = function() {
            element.remove()
        }
    }

    const list = document.querySelector('ul');
    list.addEventListener('click', function(ev) {
        if (ev.target.tagName === 'LI') ev.target.classList.toggle('checked');
    }, false);
});

function handleKeyPress(event) {
    if (event.keyCode === 13) newElement()
}

// Create a new list item when clicking on the "Add" button
function newElement() {
    const li = document.createElement("li");
    const inputValue = document.getElementById("myInput").value;
    const t = document.createTextNode(inputValue);
    li.appendChild(t);
    if (inputValue === '') {
        alert("You must write something!");
    } else {
        document.getElementById("myUL").appendChild(li);
    }
    document.getElementById("myInput").value = "";

    const span = document.createElement("SPAN");
    const txt = document.createTextNode("\u00D7");
    span.className = "close";
    span.appendChild(txt);
    li.appendChild(span);
    span.onclick = function() {
        li.remove()
    }
}