export const VAR_MAX_LENGTH = 50;

export function validateEmptyAndMax(value) {
  if (value.trim() === '') {
    return 'Gene symbol is required.';
  }

  if (value.length === VAR_MAX_LENGTH) {
    return `Maximum length (${VAR_MAX_LENGTH}) reached.`;
  }

  return '';
}
