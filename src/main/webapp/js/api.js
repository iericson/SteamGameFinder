const CARDS_PER_ROW = 40;


// Get a page of categories
async function getCategories(limit, offset) {
    const response = await fetch(`api/categories?limit=${limit}&offset=${offset}`);

    return await response.json();
}


// Get games for homepage row
async function getGamesForCategory(category) {
    const response = await fetch(
        `api/games?category=${encodeURIComponent(category)}&limit=${CARDS_PER_ROW}&primaryOnly=true`
    );

    return await response.json();
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