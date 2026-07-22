export const VAR_MAX_LENGTH = 50;
export const MAX_TARANSLINE_NAME = 100;

export function validateEmptyAndMax(value, max = VAR_MAX_LENGTH) {
  if (value.trim() === '') {
    return 'Non empty symbol is required.';
  }

  if (value.length >= max) {
    return `Maximum length (${max}) reached.`;
  }

  return '';
}
