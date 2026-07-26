import { FIRST, SECOND } from '../utils/const';
import { VAR_MAX_LENGTH, validateRequiredAndMaxLength, validateNonLessThenZeroAndRequiredAndMaxLength } from '../validation/validation'

function warningMessage(
  label,
  value,
  type = 'text',
  maxLength = VAR_MAX_LENGTH
) {
  const validate =
    type === 'select'
      ? validateNonLessThenZeroAndRequiredAndMaxLength
      : validateRequiredAndMaxLength;

  return validate(
    {
      name: label,
      value
    },
    maxLength
  );
}

export function createFieldWarning(
  first,
  second,
  firstLabel,
  secondLabel,
  firstType = 'select',
  secondType = 'select',
  firstMaxLength = VAR_MAX_LENGTH,
  secondMaxLength = VAR_MAX_LENGTH,
  warningKey = FIRST | SECOND
) {
  const firstWarning =
    (warningKey & FIRST) !== 0
      ? warningMessage(
          firstLabel,
          first,
          firstType,
          firstMaxLength
        )
      : '';

  const secondWarning =
    (warningKey & SECOND) !== 0
      ? warningMessage(
          secondLabel,
          second,
          secondType,
          secondMaxLength
        )
      : '';

  if (firstWarning && secondWarning) {
    return 'Please correct both field values.';
  }

  return firstWarning || secondWarning;
}