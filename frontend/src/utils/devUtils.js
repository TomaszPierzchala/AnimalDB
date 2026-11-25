export function conditionalClearStorage(STORAGE_KEY, clear = false) {
    if (!clear) return;

    localStorage.removeItem(STORAGE_KEY);
}