const CARDS_PER_ROW = 40;
const KNOWN_POOL_SIZE = 60;
const DISCOVERY_POOL_SIZE = 20;
const DISCOVERY_OFFSET = 100;

// Add parameter for toggling adult content
function adultParam() {
    return `adult=${localStorage.getItem("showAdultContent") === "true"}`;
}

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
    const response = await fetch(`api/categories?limit=${limit}&offset=${offset}&${adultParam()}`)

    return await response.json();
}

// Get games for homepage row. Sorted by popularity and randomized "Discovery Pool"
async function getGamesForCategory(category) {
    const [popularResponse, discoveryResponse] = await Promise.all([
        fetch(`api/games?category=${encodeURIComponent(category)}&limit=${KNOWN_POOL_SIZE}&offset=0&${adultParam()}`),
        fetch(`api/games?category=${encodeURIComponent(category)}&limit=${DISCOVERY_POOL_SIZE}&offset=${DISCOVERY_OFFSET}&${adultParam()}`)
    ]);

    const popular = await popularResponse.json();
    const discovery = await discoveryResponse.json();

    return shuffle([...popular, ...discovery]).slice(0, CARDS_PER_ROW);
}

// Get top games overall for the Popular row (not shuffled, true ranking)
async function getPopularGames() {
    const response = await fetch(
        `api/games?category=Popular&limit=${CARDS_PER_ROW}&offset=0&${adultParam()}`
    );

    return await response.json();
}

// Get games for category page
async function getGamesPage(category, limit, offset) {
    const response = await fetch(
        `api/games?category=${encodeURIComponent(category)}&limit=${limit}&offset=${offset}&${adultParam()}`
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