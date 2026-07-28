const CARDS_PER_ROW = 40;


// Get all genres

async function getGenres() {

    const response = await fetch("api/genres");

    return await response.json();
}


// Get games for homepage row

async function getGamesForGenre(genre) {

    const response = await fetch(
        `api/games?genre=${encodeURIComponent(genre)}&limit=${CARDS_PER_ROW}`
    );

    return await response.json();
}


// Get games for genre page

async function getGamesPage(genre, limit, offset) {

    const response = await fetch(
        `api/games?genre=${encodeURIComponent(genre)}&limit=${limit}&offset=${offset}`
    );

    return await response.json();
}