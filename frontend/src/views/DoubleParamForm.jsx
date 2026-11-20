import {FIRST, SECOND, VAR_MAX_LENGTH} from '../utils/const';

function FormField({
  label,
  type = 'text',
  value,
  options = [],
  placeholder = 'Select from drop-down menu',
  maxLength = VAR_MAX_LENGTH,
  onChange,
  warning = '',
  inputShowPlaceholder=false
}) {
  return (
    <div>
      <label>
        {label}:

        {type === 'select' ? (
          <select
            value={value}
            onChange={event => onChange(event.target.value)}
          >
            <option value="-1">
              {placeholder}
            </option>

            {options.map(option => (
              <option
                key={option.value}
                value={option.value}
              >
                {option.label}
              </option>
            ))}
          </select>
        ) : (
          <input
            type="text"
            value={value}
            placeholder={inputShowPlaceholder ? '—' : undefined}
            title={inputShowPlaceholder ? 'long dash when null' : undefined}
            maxLength={maxLength}
            onChange={event => onChange(event.target.value)}
          />
        )}

        {warning && (
          <small className="field-warning pulsing-text">
            {warning}
          </small>
        )}
      </label>
    </div>
  );
}

export default function DoubleParamForm({
  editing,
  entityName,

  firstLabel,
  firstType = 'text',
  firstValue,
  firstOptions = [],
  firstPlaceholder,
  firstMaxLength = VAR_MAX_LENGTH,
  firstWarning = '',

  secondLabel,
  secondType = 'text',
  secondValue,
  secondOptions = [],
  secondPlaceholder,
  secondMaxLength = VAR_MAX_LENGTH,
  secondWarning = '',

  nullAllowedKey,

  onChangeFirstField,
  onChangeSecondField,
  onSubmit,
  onDelete,
  onCancel,

  deleteArmed = false,
  isSaveEnabled = true
}) {
  return (
    <div className="popup-backdrop">
      <div className="popup">
        <h2>
          {editing
            ? `Edit ${entityName}`
            : `Add ${entityName}`}
        </h2>

        <form onSubmit={onSubmit}>
          <FormField
            label={firstLabel}
            type={firstType}
            value={firstValue}
            options={firstOptions}
            placeholder={firstPlaceholder}
            maxLength={firstMaxLength}
            warning={firstWarning}
            onChange={onChangeFirstField}
            inputShowPlaceholder={(nullAllowedKey & FIRST) !== 0}
          />

          <FormField
            label={secondLabel}
            type={secondType}
            value={secondValue}
            options={secondOptions}
            placeholder={secondPlaceholder}
            maxLength={secondMaxLength}
            warning={secondWarning}
            onChange={onChangeSecondField}
            inputShowPlaceholder={(nullAllowedKey & SECOND) !== 0}
          />

          <div className="popup-buttons">
            <div>
              {editing && onDelete && (
                <button
                  type="button"
                  className={
                    deleteArmed
                      ? 'delete-button delete-armed'
                      : 'delete-button'
                  }
                  onClick={onDelete}
                >
                  {deleteArmed
                    ? 'Confirm delete'
                    : 'Delete'}
                </button>
              )}
            </div>

            <div className="popup-main-buttons">
              <button
                type="submit"
                disabled={editing && !isSaveEnabled}
              >
                {editing ? 'Save' : 'Add'}
              </button>

              <button
                type="button"
                onClick={onCancel}
              >
                Cancel
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}