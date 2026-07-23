import {
  VAR_MAX_LENGTH,
  MAX_TARANSLINE_NAME,
  validateEmptyAndMax
} from './textValidation';

function DoubleParamForm({
  editing,
  entityName,
  firstName,
  firstValue,
  secondName,
  secondValue,

  firstInputType,
  firstOptions,

  warning,
  deleteArmed,
  hasChanges,
  onChangeFirstField,
  onChangeSecondField,
  onSubmit,
  onDelete,
  onCancel
}) {
  return (
    <div className="popup-backdrop">
      <div className="popup">
        <h2>
          {editing === null
            ? `Add ${entityName}`
            : `Edit ${entityName}`}
        </h2>

        <form onSubmit={onSubmit}>
          <div>
            <label>
              {firstName}:

              {firstInputType === 'select' ? (
                <select className='strain-select'
                  value={firstValue}
                  onChange={event =>
                    onChangeFirstField(event.target.value)
                  }
                >
                  <option value="">
                    Select {firstName}
                  </option>

                  {firstOptions.map(option => (
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
                  value={firstValue}
                  onChange={event => {
                    const value = event.target.value;

                    onChangeFirstField(
                      value,
                      validateEmptyAndMax(value)
                    );
                  }}
                  maxLength={VAR_MAX_LENGTH}
                />
              )}
              <small className="field-warning pulsing-text">
                {warning}
              </small>

            </label>
          </div>

          <div>
            <label>
              {secondName}:

              <input
                type="text"
                value={secondValue}
                onChange={firstInputType !== 'select' ? (
                  event => onChangeSecondField(event.target.value)
                  ) : (
                  event => {
                            const value = event.target.value;

                            onChangeSecondField(
                              value,
                              validateEmptyAndMax(value, MAX_TARANSLINE_NAME)
                            );
                           }
                  )
                }
                maxLength={firstInputType === 'select' ? MAX_TARANSLINE_NAME : undefined}
              />
            </label>
          </div>

          <div className="popup-buttons">
            <div>
              {editing !== null && (
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
                disabled={
                  editing !== null &&
                  !hasChanges
                }
              >
                {editing === null
                  ? 'Add'
                  : 'Save'}
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

export default DoubleParamForm;
