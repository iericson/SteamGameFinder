
const container = document.getElementById('categoryContainer');


async function buildCategoryRow(category, index) {
    const section = document.createElement('section');
    section.className = 'mb-4';

    const heading = document.createElement('a');
    heading.className = 'text-light text-decoration-none';
    heading.href = 'category.html?name=' + encodeURIComponent(category);
    const h4 = document.createElement('h4');
    h4.textContent = category;
    heading.appendChild(h4);
    section.appendChild(heading);

    const wrapper = document.createElement('div');
    wrapper.className = 'category-row-wrapper';

    const row = document.createElement('div');
    row.className = 'category-row';
    row.id = 'row-' + index;

    const games = await getGamesForCategory(category);
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
    const categories = await getCategories();

    for (let i = 0; i < categories.length; i++) {
        const section = await buildCategoryRow(categories[i], i);
        container.appendChild(section);
    }

    attachGameCardClickListener(container);
}

init();