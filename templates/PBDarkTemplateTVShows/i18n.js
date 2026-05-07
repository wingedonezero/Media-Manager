const I18N_STORAGE_KEY = 'pb-lang';

const translations = {
    en: {
        'page-title':          'Movies & TV Shows',
        'nav-movies':          'Movies',
        'nav-tvshows':         'TV Shows',
        'movies-title':        'Movies',
        'tvshows-title':       'TV Shows',
        'search-placeholder':  'search by title',
        'seasons':             'Seasons:',
        'episodes':            'Episodes:',
        'studio':              'Studio:',
        '--i18n-runtime-unit': '"min"',
        '--i18n-genres':       '"Genres: "',
        '--i18n-director':     '"Director: "',
        '--i18n-videoinfo':    '"Movie format: "',
        '--i18n-audioinfo':    '"Audio format: "',
        '--i18n-epformat':     '"Format: "',
        'cast':                'Cast',
        'season':              'Season',
    },
    fr: {
        'page-title':          'Liste des films et séries TV',
        'nav-movies':          'Films',
        'nav-tvshows':         'S\u00e9ries',
        'movies-title':        'Films',
        'tvshows-title':       'S\u00e9ries',
        'search-placeholder':  'recherche par titre',
        'seasons':             'Saisons\u00a0:',
        'episodes':            '\u00c9pisodes\u00a0:',
        'studio':              'Studio\u00a0:',
        '--i18n-runtime-unit': '"min"',
        '--i18n-genres':       '"Genres\u00a0: "',
        '--i18n-director':     '"R\u00e9alisateur\u00a0: "',
        '--i18n-videoinfo':    '"Format vid\u00e9o\u00a0: "',
        '--i18n-audioinfo':    '"Format audio\u00a0: "',
        '--i18n-epformat':     '"Format\u00a0: "',
        'cast':                'Acteurs',
        'season':              'Saison',
    },
    es: {
        'page-title':          'Pel\u00edculas y series de TV',
        'nav-movies':          'Pel\u00edculas',
        'nav-tvshows':         'Series',
        'movies-title':        'Pel\u00edculas',
        'tvshows-title':       'Series',
        'search-placeholder':  'buscar por t\u00edtulo',
        'seasons':             'Temporadas:',
        'episodes':            'Episodios:',
        'studio':              'Estudio:',
        '--i18n-runtime-unit': '"min"',
        '--i18n-genres':       '"G\u00e9neros: "',
        '--i18n-director':     '"Director: "',
        '--i18n-videoinfo':    '"Formato de v\u00eddeo: "',
        '--i18n-audioinfo':    '"Formato de audio: "',
        '--i18n-epformat':     '"Formato: "',
        'cast':                'Reparto',
        'season':              'Temporada',
    },
    it: {
        'page-title':          'Film e serie TV',
        'nav-movies':          'Film',
        'nav-tvshows':         'Serie TV',
        'movies-title':        'Film',
        'tvshows-title':       'Serie TV',
        'search-placeholder':  'cerca per titolo',
        'seasons':             'Stagioni:',
        'episodes':            'Episodi:',
        'studio':              'Studio:',
        '--i18n-runtime-unit': '"min"',
        '--i18n-genres':       '"Generi: "',
        '--i18n-director':     '"Regista: "',
        '--i18n-videoinfo':    '"Formato video: "',
        '--i18n-audioinfo':    '"Formato audio: "',
        '--i18n-epformat':     '"Formato: "',
        'cast':                'Cast',
        'season':              'Stagione',
    },
    ru: {
        'page-title':          '\u0424\u0438\u043b\u044c\u043c\u044b \u0438 \u0441\u0435\u0440\u0438\u0430\u043b\u044b',
        'nav-movies':          '\u0424\u0438\u043b\u044c\u043c\u044b',
        'nav-tvshows':         'C\u0435\u0440\u0438\u0430\u043b\u044b',
        'movies-title':        '\u0424\u0438\u043b\u044c\u043c\u044b',
        'tvshows-title':       'C\u0435\u0440\u0438\u0430\u043b\u044b',
        'search-placeholder':  '\u043f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e',
        'seasons':             '\u0421\u0435\u0437\u043e\u043d\u044b:',
        'episodes':            '\u042d\u043f\u0438\u0437\u043e\u0434\u044b:',
        'studio':              '\u0421\u0442\u0443\u0434\u0438\u044f:',
        '--i18n-runtime-unit': '"\u043c\u0438\u043d"',
        '--i18n-genres':       '"\u0416\u0430\u043d\u0440\u044b: "',
        '--i18n-director':     '"\u0420\u0435\u0436\u0438\u0441\u0441\u0451\u0440: "',
        '--i18n-videoinfo':    '"\u0424\u043e\u0440\u043c\u0430\u0442 \u0432\u0438\u0434\u0435\u043e: "',
        '--i18n-audioinfo':    '"\u0424\u043e\u0440\u043c\u0430\u0442 \u0430\u0443\u0434\u0438\u043e: "',
        '--i18n-epformat':     '"\u0424\u043e\u0440\u043c\u0430\u0442: "',
        'cast':                '\u0410\u043a\u0442\u0451\u0440\u044b',
        'season':              '\u0421\u0435\u0437\u043e\u043d',
    },
    zh: {
        'page-title':          '\u7535\u5f71\u548c\u7535\u89c6\u8282\u76ee',
        'nav-movies':          '\u7535\u5f71',
        'nav-tvshows':         '\u5267\u96c6',
        'movies-title':        '\u7535\u5f71',
        'tvshows-title':       '\u5267\u96c6',
        'search-placeholder':  '\u6309\u6807\u9898\u641c\u7d22',
        'seasons':             '\u5b63:',
        'episodes':            '\u5267\u96c6:',
        'studio':              '\u5236\u4f5c\u516c\u53f8:',
        '--i18n-runtime-unit': '"\u5206\u949f"',
        '--i18n-genres':       '"\u7c7b\u578b: "',
        '--i18n-director':     '"\u5bfc\u6f14: "',
        '--i18n-videoinfo':    '"\u89c6\u9891\u683c\u5f0f: "',
        '--i18n-audioinfo':    '"\u97f3\u9891\u683c\u5f0f: "',
        '--i18n-epformat':     '"\u683c\u5f0f: "',
        'cast':                '\u6f14\u5458',
        'season':              '\u7b2c',
    },
    de: {
        'page-title':          'Filme und Serien',
        'nav-movies':          'Filme',
        'nav-tvshows':         'Serien',
        'movies-title':        'Filme',
        'tvshows-title':       'Serien',
        'search-placeholder':  'nach Titel suchen',
        'seasons':             'Staffeln:',
        'episodes':            'Episoden:',
        'studio':              'Studio:',
        '--i18n-runtime-unit': '"min"',
        '--i18n-genres':       '"Genres: "',
        '--i18n-director':     '"Regisseur: "',
        '--i18n-videoinfo':    '"Videoformat: "',
        '--i18n-audioinfo':    '"Audioformat: "',
        '--i18n-epformat':     '"Format: "',
        'cast':                'Besetzung',
        'season':              'Staffel',
    },
    vi: {
        'page-title':          'Phim và phim truyền hình',
        'nav-movies':          'Phim',
        'nav-tvshows':         'Phim truy\u1ec1n h\u00ecnh',
        'movies-title':        'Phim',
        'tvshows-title':       'Phim truy\u1ec1n h\u00ecnh',
        'search-placeholder':  't\u00ecm ki\u1ebfm theo t\u00ean',
        'seasons':             'M\u00f9a:',
        'episodes':            'T\u1eadp:',
        'studio':              'H\u00e3ng phim:',
        '--i18n-runtime-unit': '"ph\u00fat"',
        '--i18n-genres':       '"Th\u1ec3 lo\u1ea1i: "',
        '--i18n-director':     '"\u0110\u1ea1o di\u1ec5n: "',
        '--i18n-videoinfo':    '"\u0110\u1ecbnh d\u1ea1ng video: "',
        '--i18n-audioinfo':    '"\u0110\u1ecbnh d\u1ea1ng \u00e2m thanh: "',
        '--i18n-epformat':     '"\u0110\u1ecbnh d\u1ea1ng: "',
        'cast':                'Di\u1ec5n vi\u00ean',
        'season':              'M\u00f9a',
    },
};

const CSS_VARS = [
    '--i18n-runtime-unit',
    '--i18n-genres',
    '--i18n-director',
    '--i18n-videoinfo',
    '--i18n-audioinfo',
    '--i18n-epformat',
];

function detectLanguage() {
    const browserLang = (navigator.language || '').slice(0, 2).toLowerCase();
    if (!translations[browserLang]) return 'en';
    const stored = localStorage.getItem(I18N_STORAGE_KEY);
    if (stored && translations[stored]) return stored;
    return browserLang;
}

function setLanguage(lang) {
    localStorage.setItem(I18N_STORAGE_KEY, lang);
}

function clearLanguage() {
    localStorage.removeItem(I18N_STORAGE_KEY);
}

function getAvailableLanguages() {
    return Object.keys(translations);
}

function t(key, lang) {
    const dict = translations[lang || detectLanguage()] || translations.en;
    return dict[key] || translations.en[key] || key;
}

function applyI18n(lang) {
    const l = lang || detectLanguage();
    const dict = translations[l] || translations.en;

    CSS_VARS.forEach(v => {
        if (dict[v]) document.documentElement.style.setProperty(v, dict[v]);
    });

    if (dict['page-title']) document.title = dict['page-title'];

    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) el.textContent = dict[key];
    });

    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        const key = el.getAttribute('data-i18n-placeholder');
        if (dict[key]) el.placeholder = dict[key];
    });
}

document.addEventListener('DOMContentLoaded', () => {
    applyI18n();
    if (window !== window.parent) {
        window.parent.postMessage({ type: 'pb-lang-request' }, '*');
    }
});

window.addEventListener('storage', (e) => {
    if (e.key === I18N_STORAGE_KEY) applyI18n(e.newValue);
});

window.addEventListener('message', (e) => {
    if (e.data && e.data.type === 'pb-lang') applyI18n(e.data.lang);
});
