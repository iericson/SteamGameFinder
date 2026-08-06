/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

const BATCH_SIZE = 40;

const params = new URLSearchParams(window.location.search);
const searchQuery = params.get("q");
const categoryName = params.get("name") || "Unknown";

document.getElementById("gridTitle").textContent = searchQuery ? `Search: "${searchQuery}"` : categoryName;

const grid = document.getElementById("gameGrid");
const sentinel = document.getElementById("gridSentinel");

let offset = 0;
let loading = false;
let done = false;

async function loadNextBatch() {
    if (loading || done) return;

    loading = true;

    let games;

    try {
        games = searchQuery
            ? await getSearchResults(searchQuery, BATCH_SIZE, offset)
            : await getGamesPage(categoryName, BATCH_SIZE, offset);
    } catch (error) {
        console.error("Failed loading games:", error);
        loading = false;
        return;
    }
    
    if (!Array.isArray(games)) {
        console.error("Unexpected response loading games:", games);
        loading = false;
        return;
    }

    if (games.length === 0) {
        done = true;
        loading = false;
        return;
    }

    games.forEach(game => grid.appendChild(buildCard(game)));

    offset += games.length;
    loading = false;

    const rect = sentinel.getBoundingClientRect();
    if (rect.top < window.innerHeight + 800) {
        loadNextBatch();
    }
}

function handleIntersection(entries) {
    if (entries[0].isIntersecting) {
        loadNextBatch();
    }
}

const observer = new IntersectionObserver(handleIntersection, { rootMargin: "800px" });
observer.observe(sentinel);

loadNextBatch();
attachGameCardClickListener(grid);
