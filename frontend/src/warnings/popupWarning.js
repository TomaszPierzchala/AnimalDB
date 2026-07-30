import { FIRST, SECOND, VAR_MAX_LENGTH } from '../utils/const';
import { validateRequiredAndMaxLength, validateNonLessThenZeroAndRequiredAndMaxLength } from '../validation/validation'

function warningMessage(
  label,
  value,
  type = 'text',
  maxLength
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
  warningKey = FIRST | SECOND,
  firstMaxLength = VAR_MAX_LENGTH,
  secondMaxLength = VAR_MAX_LENGTH
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