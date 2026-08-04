/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

const grid = document.getElementById("gameGrid");
const emptyMessage = document.getElementById("emptyMessage");

async function loadMyList() {
    let games;

    try {
        games = await getMyList();
    } catch (error) {
        console.error("Failed loading My List:", error);
        return;
    }

    grid.innerHTML = "";

    if (games.length === 0) {
        emptyMessage.classList.remove("d-none");
        return;
    }

    emptyMessage.classList.add("d-none");

    games.forEach(game => grid.appendChild(buildCard(game)));
}

attachGameCardClickListener(grid);

// Refresh the grid whenever the modal closes, in case a game was removed
document.getElementById("gameModal").addEventListener("hidden.bs.modal", loadMyList);

loadMyList();
