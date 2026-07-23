export const VAR_MAX_LENGTH = 50;
export const MAX_TARANSLINE_NAME = 100;

export function validateRequiredAndMaxLength(value, maxLength = VAR_MAX_LENGTH) {
  const text = String(value ?? '');

  if (text.trim() === '') {
    return 'Value is required.';
  }

  if (text.length >= maxLength) {
    return `Maximum length (${maxLength}) exceeded.`;
  }

  return '';
}

export function validateNonLessThenZeroAndRequiredAndMaxLength(value, max = VAR_MAX_LENGTH) {
  if (Number(value)<0) {
	return 'Select a value from the drop-down menue';
  }
  return validateRequiredAndMaxLength(value, max);
}
