import { VAR_MAX_LENGTH } from '../utils/const';

function FormField({
  name,
  type = 'text',
  value,
  options = [],
  placeholder = 'Select from drop-down menu',
  maxLength = VAR_MAX_LENGTH,
  onChange,
  warning = ''
}) {
  return (
    <div>
      <label>
        {name}:

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

  firstName,
  firstType = 'text',
  firstValue,
  firstOptions = [],
  firstPlaceholder,
  firstMaxLength = VAR_MAX_LENGTH,
  firstWarning = '',

  secondName,
  secondType = 'text',
  secondValue,
  secondOptions = [],
  secondPlaceholder,
  secondMaxLength = VAR_MAX_LENGTH,
  secondWarning = '',

  onChangeFirstField,
  onChangeSecondField,
  onSubmit,
  onDelete,
  onCancel,

  deleteArmed = false,
  hasChanges = true
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
            name={firstName}
            type={firstType}
            value={firstValue}
            options={firstOptions}
            placeholder={firstPlaceholder}
            maxLength={firstMaxLength}
            warning={firstWarning}
            onChange={onChangeFirstField}
          />

          <FormField
            name={secondName}
            type={secondType}
            value={secondValue}
            options={secondOptions}
            placeholder={secondPlaceholder}
            maxLength={secondMaxLength}
            warning={secondWarning}
            onChange={onChangeSecondField}
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
                disabled={editing && !hasChanges}
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