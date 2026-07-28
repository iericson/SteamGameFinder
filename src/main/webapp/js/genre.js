const BATCH_SIZE = 40;

const params = new URLSearchParams(window.location.search);
const genreName = params.get("name") || "Unknown";

document.getElementById("gridTitle").textContent = genreName;

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
        games = await getGamesPage(genreName, BATCH_SIZE, offset);
    } catch (error) {
        console.error("Failed loading games:", error);
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
}

function handleIntersection(entries) {
    if (entries[0].isIntersecting) {
        loadNextBatch();
    }
}

const observer = new IntersectionObserver(handleIntersection);
observer.observe(sentinel);

loadNextBatch();
attachGameCardClickListener(grid);