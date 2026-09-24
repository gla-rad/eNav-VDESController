/**
 * The key under which the selected colour theme is remembered.
 */
const THEME_STORAGE_KEY = 'vdes-ctrl-theme';

/**
 * Returns the colour theme currently applied to the document.
 *
 * @return {String} Either "light" or "dark"
 */
function getTheme() {
    return document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
}

/**
 * Applies the provided colour theme to the document and remembers the choice
 * for the next visit.
 *
 * @param {String}  theme   The theme to apply ("light" or "dark")
 */
function setTheme(theme) {
    const applied = theme === 'dark' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-bs-theme', applied);
    try {
        window.localStorage.setItem(THEME_STORAGE_KEY, applied);
    } catch (e) {
        // Storage may be unavailable (private mode), the theme still applies
    }
}

/**
 * A helper function sets a value to null if it's empty/undefined.
 *
 * @param {*} obj   The object to be checked whether empty
 */
function nullIfEmpty(obj) {
    if (obj && obj != "null" && obj != "undefined") {
        return obj;
    }
    return null;
}

/**
 * A helper function to handle error UI operations.
 *
 * @param {String}      text    The error text to be displayed
 */
function showErrorDialog(text, action) {
    // Initialise the confirmation dialog
    $('#errorDialog .modal-body').html(text || 'An unexpected error occurred.');

    // And show the dialog
    $('#errorDialog').modal('show');
}

/**
 * The default Datatables presentation options shared by the tables of the
 * application. Individual tables merge their own ajax source, columns,
 * buttons and callbacks on top of these using $.extend().
 *
 * @return {Object} The common Datatables configuration
 */
function commonDatatableOptions() {
    return {
        select: 'single',
        responsive: true,
        autoWidth: false,
        lengthMenu: [10, 25, 50, 75, 100],
        language: {
            search: '',
            searchPlaceholder: 'Search…',
            lengthMenu: 'Show _MENU_',
            info: '_START_–_END_ of _TOTAL_',
            infoEmpty: 'No entries',
            infoFiltered: '(filtered from _MAX_)',
            emptyTable: 'Nothing to show here yet',
            zeroRecords: 'No entry matches the current search',
            processing: '<i class="fa-solid fa-circle-notch fa-spin"></i>&nbsp;Loading…'
        },
        layout: {
            topStart: 'buttons',
            topEnd: 'search',
            bottomStart: ['pageLength', 'info'],
            bottomEnd: 'paging'
        }
    };
}

/**
 * Wires up the elements that are present on every page - currently just the
 * colour theme toggle sitting in the navigation bar.
 */
$(() => {
    $('[data-theme-toggle]').on('click', () => setTheme(getTheme() === 'dark' ? 'light' : 'dark'));
});
