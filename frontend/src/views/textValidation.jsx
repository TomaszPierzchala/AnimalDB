import { firstCapital } from '../utils/textUtils'

export const VAR_MAX_LENGTH = 50;
export const MAX_TARANSLINE_NAME = 100;

export function validateRequiredAndMaxLength(
  { name = 'Value', value },
  maxLength = VAR_MAX_LENGTH
) {
  const text = String(value ?? '');

  if (text.trim() === '') {
    return `${firstCapital(name)} is required.`;
  }

  if (text.length >= maxLength) {
    return `${firstCapital(name)}: maximum length (${maxLength}) exceeded.`;
  }

  return '';
}

export function validateNonLessThenZeroAndRequiredAndMaxLength(
  { name = 'Value', value },
  maxLength = VAR_MAX_LENGTH) {
  if (Number(value)<0) {
	return `Select a ${firstCapital(name)} from the drop-down menue`;
  }
  return validateRequiredAndMaxLength({name: name, value: value}, maxLength);
}
