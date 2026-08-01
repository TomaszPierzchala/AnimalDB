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

export function isCurrentInitialPopupStateChosen(first, second, dropDown = FIRST|SECOND, warningKey = FIRST|SECOND) {
  const firstIsRequired =
    (warningKey & FIRST) !== 0;

  const firstIsDropDown =
    (dropDown & FIRST) != 0;

  const secondIsRequired =
    (warningKey & SECOND) !== 0;

  const secondIsDropDown =
	  (dropDown & SECOND) != 0;

  const firstIsChosen =
    !firstIsRequired ||
    (
      first !== null &&
      first !== undefined &&
      String(first).trim() !== '' &&
      (!firstIsDropDown || String(first) !== '-1' ) // firstIsDropDown => (String(first) !== '-1')
    );

  const secondIsChosen =
    !secondIsRequired ||
    (
      second !== null &&
      second !== undefined &&
      String(second).trim() !== '' &&
      (!secondIsDropDown || String(second) !== '-1' ) // secondIsDropDown => (String(second) !== '-1')
    );

  return firstIsChosen && secondIsChosen;
}
