const CARDS_PER_ROW = 40;
const CATEGORY_POOL_SIZE = 100;

// Shuffle arrays to randomize games in category lists
function shuffle(array) {
    const result = array.slice();
    for (let i = result.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [result[i], result[j]] = [result[j], result[i]];
    }
    return result;
}


// Get a page of categories
async function getCategories(limit, offset) {
    const response = await fetch(`api/categories?limit=${limit}&offset=${offset}`);

    return await response.json();
}


// Get games for homepage row
async function getGamesForCategory(category) {
    const response = await fetch(
        `api/games?category=${encodeURIComponent(category)}&limit=${CATEGORY_POOL_SIZE}&offset=0&primaryOnly=true`
    );

    const games = await response.json();
    return shuffle(games).slice(0, CARDS_PER_ROW);
}


// Get games for category page
async function getGamesPage(category, limit, offset) {
    const response = await fetch(
        `api/games?category=${encodeURIComponent(category)}&limit=${limit}&offset=${offset}`
    );

    return await response.json();
}


// Get the games saved to this browser session's My List
async function getMyList() {
    const response = await fetch("api/mylist");

    return await response.json();
}


// Save a game to this browser session's My List
async function addToMyList(id) {
    const response = await fetch(`api/mylist?id=${encodeURIComponent(id)}`, {
        method: "POST"
    });

    return await response.json();
}


// Remove a game from this browser session's My List
async function removeFromMyList(id) {
    const response = await fetch(`api/mylist?id=${encodeURIComponent(id)}`, {
        method: "DELETE"
    });

    return await response.json();
}