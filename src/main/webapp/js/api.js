const CARDS_PER_ROW = 40;


// Get all categories
async function getCategories() {
    const response = await fetch("api/categories");

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