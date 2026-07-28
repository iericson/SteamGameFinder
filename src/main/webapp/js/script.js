
const container = document.getElementById('genreContainer');


async function buildBrowseRow(genre, index) {
    const section = document.createElement('section');
    section.className = 'mb-4';

    const heading = document.createElement('a');
    heading.className = 'text-light text-decoration-none';
    heading.href = 'genre.html?name=' + encodeURIComponent(genre);
    const h4 = document.createElement('h4');
    h4.textContent = genre;
    heading.appendChild(h4);
    section.appendChild(heading);

    const wrapper = document.createElement('div');
    wrapper.className = 'genre-row-wrapper';

    const row = document.createElement('div');
    row.className = 'genre-row';
    row.id = 'row-' + index;

    const games = await getGamesForGenre(genre);
    games.forEach(game => row.appendChild(buildCard(game)));

    // scrolls by however many cards are currently visible
    function scrollByVisibleCards(direction) {
        const firstCard = row.querySelector('.game-card');
        if (!firstCard) return;
        const cardWidth = firstCard.getBoundingClientRect().width;
        const gap = parseFloat(getComputedStyle(row).gap) || 0;
        const visibleCount = Math.max(1, Math.round(row.clientWidth / (cardWidth + gap)));
        const distance = visibleCount * (cardWidth + gap);
        row.scrollBy({ left: direction * distance, behavior: 'smooth' });
    }

    const leftArrow = document.createElement('button');
    leftArrow.className = 'row-arrow left';
    leftArrow.innerHTML = '&#10094;';
    leftArrow.addEventListener('click', () => scrollByVisibleCards(-1));

    const rightArrow = document.createElement('button');
    rightArrow.className = 'row-arrow right';
    rightArrow.innerHTML = '&#10095;';
    rightArrow.addEventListener('click', () => scrollByVisibleCards(1));

    wrapper.appendChild(row);
    wrapper.appendChild(leftArrow);
    wrapper.appendChild(rightArrow);
    section.appendChild(wrapper);
    return section;
}

// ---- page init ----
async function init() {
    const genres = await getGenres();

    for (let i = 0; i < genres.length; i++) {
        const section = await buildBrowseRow(genres[i], i);
        container.appendChild(section);
    }

    attachGameCardClickListener(container);
}

init();