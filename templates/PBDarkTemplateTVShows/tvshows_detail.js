document.addEventListener('DOMContentLoaded', function() {
    const wrapper = document.querySelector('.episodes.wrapper');
    if (!wrapper) return;

    const episodes = Array.from(wrapper.querySelectorAll('.episode.outerContainer'));
    if (episodes.length === 0) return;

    const seasons = new Map();
    episodes.forEach(function(ep) {
        const titleEl = ep.querySelector('.episode.epTitle');
        const text = titleEl ? titleEl.textContent.trim() : '';
        const m = text.match(/^(\d+)x/);
        const s = m ? parseInt(m[1], 10) : 0;
        if (!seasons.has(s)) seasons.set(s, []);
        seasons.get(s).push(ep);
    });

    wrapper.innerHTML = '';

    seasons.forEach(function(eps, seasonNum) {
        const section = document.createElement('div');
        section.className = 'season-section';

        const cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.id = 'season-' + seasonNum;
        cb.checked = true;
        section.appendChild(cb);

        const label = document.createElement('label');
        label.className = 'season-toggle-label';
        label.setAttribute('for', 'season-' + seasonNum);

        const span = document.createElement('span');
        span.setAttribute('data-i18n', 'season');
        span.textContent = 'Season';
        label.appendChild(span);
        label.appendChild(document.createTextNode('\u00a0' + seasonNum));
        section.appendChild(label);

        const div = document.createElement('div');
        div.className = 'season-episodes';
        eps.forEach(function(ep) { div.appendChild(ep); });
        section.appendChild(div);

        wrapper.appendChild(section);
    });

    applyI18n();
});
