export const SYMBOL_MAX_LENGTH = 50;

export function validateSymbol(value) {
  if (value.trim() === '') {
    return 'Gene symbol is required.';
  }

  if (value.length === SYMBOL_MAX_LENGTH) {
    return `Maximum length (${SYMBOL_MAX_LENGTH}) reached.`;
  }

  return '';
}
